import hashlib
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import release
from version import Version, write_version

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
        state = {'repository': release.REPOSITORY, 'base': self.base, 'commit': self.git('rev-parse', 'HEAD'), 'version': '0.1.0', 'build': 1000, 'branch': 'release-candidate/test'}
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
    def test_missing_ci_checkpoint_rejected(self):
        with self.assertRaisesRegex(RuntimeError, 'no successful CI'): release.verify_candidate_ci(self.candidate())
    def test_download_redirect_drops_github_credentials(self):
        import urllib.request
        request = urllib.request.Request('https://api.github.com/asset', headers={'Authorization':'Bearer test'})
        redirected = release.SafeRedirect().redirect_request(request, None, 302, '', {}, 'https://release-assets.githubusercontent.com/file')
        self.assertIsNone(redirected.get_header('Authorization'))

if __name__ == '__main__': unittest.main()
