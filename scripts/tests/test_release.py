import hashlib
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
import zipfile
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import release
from version import Version, write_version
from web_assets import copy_hashed_bundle, distribution_hash, inventory

class ReleaseGuardsTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.assets = self.root / '.release/artifacts'; self.assets.mkdir(parents=True)
        self.context = patch.multiple(release, ROOT=self.root, STATE=self.root / '.release/state.json', ASSETS=self.assets)
        self.context.start()
        self.git('init', '-b', 'main')
        self.git('config', 'commit.gpgSign', 'false'); self.git('config', 'user.name', 'Test'); self.git('config', 'user.email', 'test@example.test')
        (self.root / 'version.properties').write_text('VERSION=0.0.0\nBUILD_NUMBER=0\n')
        self.git('add', '.'); self.git('commit', '-m', 'baseline')
        self.base = self.git('rev-parse', 'HEAD')
    def tearDown(self):
        self.context.stop(); self.temp.cleanup()
    def git(self, *args):
        return subprocess.check_output(['git', *args], cwd=self.root, stderr=subprocess.DEVNULL, text=True).strip()
    def candidate(self, extra=False):
        write_version(self.root / 'version.properties', Version.parse('0.1.0'))
        if extra: (self.root / 'implementation.kt').write_text('unexpected')
        self.git('add', 'version.properties')
        if extra: self.git('add', 'implementation.kt')
        self.git('commit', '-m', 'release')
        state = {'repository': release.REPOSITORY, 'base': self.base, 'commit': self.git('rev-parse', 'HEAD'), 'version': '0.1.0', 'build': 100, 'branch': 'release-candidate/test'}
        release.write_state(state); return state
    def test_public_download_inventory_is_android_only(self):
        self.assertEqual(release.asset_names(self.candidate()), ["domposable-0.1.0.apk", "website.zip", "maven-bundle.zip"])
    def test_plain_semver_release_is_published_as_latest(self):
        state = self.candidate()
        with patch.object(release, 'verify_assets'), patch.object(release, 'GitHub') as github, patch.object(release, 'checkpoint'), patch.object(release, 'extract_website'):
            github.return_value.request.return_value = {'id': 9}
            release.stage()
            created = github.return_value.request.call_args.args[2]
            self.assertEqual(created['tag_name'], 'v0.1.0')
            self.assertFalse(created['prerelease'])
            state.update(release_id=9, central_state='PUBLISHED')
            release.write_state(state)
            release.publish_github()
            published = github.return_value.request.call_args.args[2]
            self.assertFalse(published['draft'])
            self.assertEqual(published['make_latest'], 'true')
            self.assertEqual(published['tag_name'], 'v0.1.0')
            self.assertEqual(published['target_commitish'], state['commit'])
            self.assertFalse(published['prerelease'])
    def failed_candidate(self):
        state = self.candidate()
        state.update(phase='staged', central_state='FAILED', deployment_id='00000000-0000-0000-0000-000000000001', release_id=9, run_id=123)
        release.write_state(state)
        return state
    def test_failed_candidate_is_archived_without_changing_immutable_assets(self):
        state = self.failed_candidate()
        with patch.object(release, 'verify_assets'), patch('publish_central.request', return_value='{"deploymentState":"FAILED"}'), patch.object(release, 'run', side_effect=[state['base']+'\trefs/heads/main', '']), patch.object(release, 'GitHub') as github, patch.object(release, 'checkpoint') as checkpoint:
            github.return_value.request.return_value = {'draft':True}
            release.abandon()
        self.assertEqual(checkpoint.call_args.args[0]['phase'], 'abandoned')
    def test_abandon_rechecks_central_failure(self):
        self.failed_candidate()
        with patch.object(release, 'verify_assets'), patch('publish_central.request', return_value='{"deploymentState":"PUBLISHED"}'):
            with self.assertRaisesRegex(RuntimeError, 'still report FAILED'): release.abandon()
    def test_abandon_rejects_main_race(self):
        self.failed_candidate()
        with patch.object(release, 'verify_assets'), patch('publish_central.request', return_value='{"deploymentState":"FAILED"}'), patch.object(release, 'run', return_value='f'*40+'\trefs/heads/main'):
            with self.assertRaisesRegex(RuntimeError, 'Main moved'): release.abandon()
    def test_abandon_rejects_existing_version_tag(self):
        state = self.failed_candidate()
        with patch.object(release, 'verify_assets'), patch('publish_central.request', return_value='{"deploymentState":"FAILED"}'), patch.object(release, 'run', side_effect=[state['base']+'\trefs/heads/main','f'*40+'\trefs/tags/v0.1.0']):
            with self.assertRaisesRegex(RuntimeError, 'version tag exists'): release.abandon()
    def test_abandon_rejects_published_release(self):
        state = self.failed_candidate()
        with patch.object(release, 'verify_assets'), patch('publish_central.request', return_value='{"deploymentState":"FAILED"}'), patch.object(release, 'run', side_effect=[state['base']+'\trefs/heads/main','']), patch.object(release, 'GitHub') as github:
            github.return_value.request.return_value = {'draft':False}
            with self.assertRaisesRegex(RuntimeError, 'Published releases'): release.abandon()
    def test_only_abandoned_drafts_are_ignored_during_preparation(self):
        state = self.failed_candidate(); state['phase'] = 'abandoned'
        archived = {'draft':True,'body':release.release_body(state)}
        self.assertTrue(release.is_abandoned_draft(archived))
        self.assertFalse(release.is_abandoned_draft({**archived,'draft':False}))
    def test_version_only_candidate(self):
        release.validate_candidate(self.candidate())
    def test_historical_candidate_retains_its_immutable_installation_code(self):
        state = self.candidate()
        (self.root / 'version.properties').write_text('VERSION=0.1.0\nBUILD_NUMBER=1\n')
        self.git('add', 'version.properties'); self.git('commit', '--amend', '--no-edit')
        state.update(commit=self.git('rev-parse', 'HEAD'), build=1)
        release.validate_candidate(state)
    def prepare_existing_candidate(self, *, legacy=False, older_source=False, candidate_version='0.1.0'):
        commit = 'a' * 40
        def git_transport(*args, capture=False):
            if args[:2] == ('git', 'rev-parse'):
                return self.base if args[-1] == 'HEAD' or not older_source else 'f' * 40
            if args[:2] == ('git', 'ls-remote'):
                return commit + '\t' + args[-1]
            if args[:3] == ('git', 'checkout', '--detach'):
                text = 'VERSION=0.0.0\n' if args[-1] == 'origin/main' else f'VERSION={candidate_version}\n'
                if args[-1] == commit and legacy: text += 'BUILD_NUMBER=1\n'
                (self.root / 'version.properties').write_text(text)
            return ''
        with patch.dict('os.environ', {'GITHUB_REF':'refs/heads/main', 'GITHUB_RUN_ID':'123'}), patch.object(release, 'GitHub') as github, patch.object(release, 'run', side_effect=git_transport), patch.object(release, 'validate_candidate') as validate, patch.object(release, 'output'):
            github.return_value.request.return_value = []
            release.prepare('minor')
            validate.assert_called_once()
        return release.read_state()
    def test_unstaged_candidate_rebuild_uses_current_version_derived_code(self):
        state = self.prepare_existing_candidate()
        self.assertEqual(state['version'], '0.1.0')
        self.assertEqual(state['build'], 100)
        self.assertEqual(state['base'], self.base)
    def test_unstaged_legacy_counter_cannot_trigger_a_rebuild(self):
        with self.assertRaisesRegex(ValueError, 'VERSION only'):
            self.prepare_existing_candidate(legacy=True)
    def test_unstaged_candidate_from_an_old_policy_cannot_trigger_a_rebuild(self):
        with self.assertRaisesRegex(RuntimeError, 'older main'):
            self.prepare_existing_candidate(older_source=True)
    def test_unstaged_candidate_must_match_the_requested_version(self):
        with self.assertRaisesRegex(RuntimeError, 'requested version'):
            self.prepare_existing_candidate(candidate_version='0.2.0')
    def test_implementation_change_rejected(self):
        with self.assertRaisesRegex(RuntimeError, 'only version'): release.validate_candidate(self.candidate(extra=True))
    def test_malformed_sha_rejected_before_git(self):
        state = self.candidate(); state['commit'] = '--upload-pack=bad'
        with self.assertRaisesRegex(RuntimeError, 'Invalid release commit'): release.validate_candidate(state)
    def test_corrupt_preserved_asset_rejected(self):
        state = self.candidate()
        for name in release.asset_names(state): (self.assets / name).write_bytes(b'original')
        (self.assets / 'manifest.json').write_text(json.dumps({'commit':state['commit'], 'version':state['version'], 'sha256':{name:hashlib.sha256(b'original').hexdigest() for name in release.asset_names(state)}}))
        release.verify_assets()
        (self.assets / 'website.zip').write_bytes(b'changed!')
        with self.assertRaisesRegex(RuntimeError, 'Corrupt'): release.verify_assets()
    def candidate_site(self, state):
        site = self.root / 'build/release-site'
        (site / 'api').mkdir(parents=True)
        (site / 'index.html').write_text('site')
        (site / 'api/index.html').write_text('api')
        (site / 'api/guide.md').write_text('guide')
        (site / 'api/web-assets.json').write_text('API content, not a bundle manifest')
        web = {}
        for name in ('demo', 'wasm'):
            source = self.root / f'{name}-source'; source.mkdir()
            (source / 'index.html').write_text('<script src="demo.js"></script>')
            (source / 'demo.js').write_text(name)
            web['html' if name == 'demo' else 'wasm'] = copy_hashed_bundle(source, site / name)
        api_files = inventory(site / 'api', exclude_manifest=False)
        (site / 'release.json').write_text(json.dumps({
            'version': state['version'], 'build': state['build'], 'commit': state['commit'],
            'status': 'release', 'api': {'sha256': distribution_hash(api_files), 'files': api_files}, 'web': web,
        }))
        return site
    def test_packages_verified_exact_candidate_site(self):
        state = self.candidate(); site = self.candidate_site(state)
        release.package_site(site)
        with zipfile.ZipFile(self.assets / 'website.zip') as archive:
            self.assertIn('release.json', archive.namelist())
            self.assertIn('api/index.html', archive.namelist())
            self.assertIn('api/guide.md', archive.namelist())
            self.assertIn('api/web-assets.json', archive.namelist())
            self.assertIn('demo/web-assets.json', archive.namelist())
            self.assertIn('wasm/web-assets.json', archive.namelist())
    def test_candidate_site_metadata_must_match_release_state(self):
        state = self.candidate(); site = self.candidate_site(state)
        metadata_path = site / 'release.json'; metadata = json.loads(metadata_path.read_text())
        for key, wrong in [('commit', 'f' * 40), ('version', '0.2.0'), ('build', state['build'] + 1), ('status', 'preview')]:
            with self.subTest(key=key):
                metadata_path.write_text(json.dumps({**metadata, key: wrong}))
                with self.assertRaisesRegex(RuntimeError, 'metadata does not match'):
                    release.package_site(site)
        metadata_path.write_text(json.dumps(metadata))
        (site / 'api/guide.md').write_text('changed guide')
        with self.assertRaisesRegex(RuntimeError, 'API tree does not match'):
            release.package_site(site)
        (site / 'api/guide.md').write_text('guide')
        (site / 'api/guide.md').unlink()
        with self.assertRaisesRegex(RuntimeError, 'API tree does not match'):
            release.package_site(site)
        (site / 'api/guide.md').write_text('guide')
        (site / 'api/web-assets.json').unlink()
        with self.assertRaisesRegex(RuntimeError, 'API tree does not match'):
            release.package_site(site)
        (site / 'api/web-assets.json').write_text('API content, not a bundle manifest')
        (site / 'demo/web-assets.json').write_text('{}')
        with self.assertRaisesRegex(RuntimeError, 'invalid html browser bundle'):
            release.package_site(site)
    def test_candidate_site_requires_api_index(self):
        state = self.candidate(); site = self.candidate_site(state)
        (site / 'api/index.html').unlink()
        with self.assertRaisesRegex(RuntimeError, 'missing api/index.html'):
            release.package_site(site)
    def test_candidate_site_rejects_symlinks(self):
        state = self.candidate(); site = self.candidate_site(state)
        (site / 'escape').symlink_to(self.root / 'outside')
        with self.assertRaisesRegex(RuntimeError, 'symbolic link'):
            release.package_site(site)
    def test_main_race_prevents_push(self):
        self.candidate()
        commands = []
        def fake(*args, capture=False):
            commands.append(args)
            if args[1] == 'ls-remote': return 'f' * 40 + '\trefs/heads/main'
            return ''
        with patch.object(release, 'validate_candidate'), patch.object(release, 'verify_assets'), patch.object(release, 'verify_candidate_ci'), patch.object(release, 'run', side_effect=fake), patch.object(release.subprocess, 'run') as result:
            result.return_value.returncode = 1
            with self.assertRaisesRegex(RuntimeError, 'Main advanced'): release.advance_main()
        self.assertFalse(any(command[1] == 'push' for command in commands))
    def test_already_advanced_main_records_checkpoint(self):
        state = self.candidate()
        with patch.object(release, 'validate_candidate'), patch.object(release, 'verify_assets'), patch.object(release, 'verify_candidate_ci'), patch.object(release, 'run', return_value=state['commit'] + '\trefs/heads/main'), patch.object(release, 'checkpoint') as checkpoint:
            release.advance_main()
        self.assertEqual(checkpoint.call_args.args[0]['phase'], 'main-advanced')
    def test_ci_checkpoint_must_be_for_the_exact_candidate(self):
        state = self.candidate(); state['ci_run_id'] = 123
        with patch.object(release, 'GitHub') as github:
            github.return_value.request.return_value = {'head_sha':'f'*40, 'conclusion':'success', 'path':'.github/workflows/ci.yml'}
            with self.assertRaisesRegex(RuntimeError, 'this commit'): release.verify_candidate_ci(state)
    def test_wait_ci_outputs_the_exact_successful_run_id(self):
        state = self.candidate()
        output = self.root / 'github-output'
        run = {'id':321, 'head_sha':state['commit'], 'head_branch':state['branch'], 'status':'completed', 'conclusion':'success'}
        with patch.dict('os.environ', {'GITHUB_OUTPUT':str(output)}), patch.object(release, 'GitHub') as github:
            github.return_value.request.return_value = {'workflow_runs':[run]}
            release.wait_ci()
        self.assertEqual(output.read_text(), 'ci_run_id=321\n')
        self.assertEqual(release.read_state()['ci_run_id'], 321)
    def test_missing_ci_checkpoint_rejected(self):
        with self.assertRaisesRegex(RuntimeError, 'no successful CI'): release.verify_candidate_ci(self.candidate())
    def test_download_redirect_drops_github_credentials(self):
        import urllib.request
        request = urllib.request.Request('https://api.github.com/asset', headers={'Authorization':'Bearer test'})
        redirected = release.SafeRedirect().redirect_request(request, None, 302, '', {}, 'https://release-assets.githubusercontent.com/file')
        self.assertIsNone(redirected.get_header('Authorization'))

if __name__ == '__main__': unittest.main()
