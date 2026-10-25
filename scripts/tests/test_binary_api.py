import importlib.util
import json
from pathlib import Path
import subprocess
import tempfile
import unittest


spec = importlib.util.spec_from_file_location('binary_api', Path(__file__).resolve().parents[1] / 'ci/binary_api.py')
api = importlib.util.module_from_spec(spec)
spec.loader.exec_module(api)


class BinaryApiTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.policy = {
            'runtime_modules': ['domposable-ui'],
            'processor_modules': ['domposable-ksp'],
            'klib_targets': ['iosArm64', 'iosSimulatorArm64', 'js', 'wasmJs'],
        }
        self.write('config/binary-api.json', json.dumps(self.policy))
        for module in ['domposable-ui', 'domposable-ksp']:
            self.write(f'{module}/build.gradle.kts', 'plugins { alias(libs.plugins.maven.publish) }')
        for path in api.expected_paths(self.policy):
            self.write(path, '// Targets: [iosArm64, iosSimulatorArm64, js, wasmJs]\nfinal class test/Ui {\n}\n' if path.endswith('.klib.api') else 'public final class test/Ui {\n\tpublic fun keep ()V\n}\n')
        self.git('init', '-q')
        self.git('config', 'user.name', 'API test')
        self.git('config', 'user.email', 'api@example.invalid')
        self.commit()
        self.git('tag', 'v0.2.0')

    def git(self, *args):
        return api.git(self.root, *args).strip()

    def commit(self):
        self.git('add', '.')
        self.git('-c', 'commit.gpgsign=false', 'commit', '-qm', 'fixture')

    def write(self, path, content):
        file = self.root / path
        file.parent.mkdir(parents=True, exist_ok=True)
        file.write_text(content)

    def test_full_inventory_and_unchanged_release(self):
        markdown = api.report(self.root, 'HEAD', 'HEAD', None, self.root / 'report')
        self.assertIn('4 ABI dumps', markdown)
        self.assertIn('No ABI baseline changes.', markdown)
        report = json.loads((self.root / 'report/report.json').read_text())
        self.assertEqual('v0.2.0', report['comparisons']['release']['ref'])
        self.assertEqual([], report['comparisons']['pr-base']['changes'])

    def test_build_tooling_modules_have_reviewed_jvm_dumps(self):
        self.policy['tooling_modules'] = ['domposable-export-gradle']
        path = 'domposable-export-gradle/api/domposable-export-gradle.api'
        self.write('domposable-export-gradle/build.gradle.kts', 'plugins { alias(libs.plugins.maven.publish) }')
        self.write(path, 'public abstract class test/ExportSite {}')
        self.assertIn(path, api.inventory(self.root, self.policy))
        (self.root / path).unlink()
        with self.assertRaisesRegex(ValueError, 'inventory mismatch'):
            api.inventory(self.root, self.policy)

    def test_removed_signatures_remain_visible_after_baseline_update(self):
        path = 'domposable-ui/api/jvm/domposable-ui.api'
        self.write(path, 'public final class test/Ui {\n}\n')
        self.commit()
        api.report(self.root, 'HEAD^', 'HEAD', None, self.root / 'report')
        report = json.loads((self.root / 'report/report.json').read_text())
        changes = report['comparisons']['release']['changes']
        self.assertEqual([{'path': path, 'added_lines': 0, 'removed_lines': 1}], changes)
        self.assertIn('-\tpublic fun keep ()V', (self.root / 'report/release.diff').read_text())

    def test_additions_and_new_modules_are_reported(self):
        changes, diff = api.compare({'old.api': 'one\n'}, {'old.api': 'one\ntwo\n', 'new.api': 'new\n'})
        self.assertEqual(2, len(changes))
        self.assertIn('+two', diff)
        self.assertIn('+new', diff)

    def test_missing_or_empty_dump_fails(self):
        path = self.root / 'domposable-ui/api/android/domposable-ui.api'
        original = path.read_text()
        path.unlink()
        with self.assertRaisesRegex(ValueError, 'inventory mismatch'):
            api.inventory(self.root, self.policy)
        path.write_text('')
        with self.assertRaisesRegex(ValueError, 'malformed'):
            api.inventory(self.root, self.policy)
        path.write_text(original)

    def test_removed_klib_target_fails(self):
        path = self.root / 'domposable-ui/api/domposable-ui.klib.api'
        path.write_text(path.read_text().replace(', wasmJs', ''))
        with self.assertRaisesRegex(ValueError, 'Incomplete Klib'):
            api.inventory(self.root, self.policy)

    def test_untracked_extra_dump_and_unlisted_publication_fail(self):
        path = self.root / 'domposable-ui/api/extra.api'
        path.write_text('public final class test/Extra {}')
        with self.assertRaisesRegex(ValueError, 'unexpected'):
            api.inventory(self.root, self.policy)
        path.unlink()
        self.write('domposable-new/build.gradle.kts', 'plugins { alias(libs.plugins.maven.publish) }')
        with self.assertRaisesRegex(ValueError, 'Published modules differ'):
            api.inventory(self.root, self.policy)

    def test_release_selection_uses_semver_and_ancestry(self):
        for tag in ['v0.9.0', 'v0.10.0', 'visual-evidence', 'v1.0.0-alpha01']:
            self.git('tag', tag)
        self.assertEqual('v0.10.0', api.latest_release(self.root, 'HEAD'))
        original = self.git('rev-parse', 'HEAD')
        self.write('future.txt', 'future')
        self.commit()
        self.git('tag', 'v2.0.0')
        self.assertEqual('v0.10.0', api.latest_release(self.root, original))

    def test_missing_tags_and_invalid_revision_fail_closed(self):
        self.git('tag', '-d', 'v0.2.0')
        with self.assertRaisesRegex(ValueError, 'Fetch full history'):
            api.latest_release(self.root, 'HEAD')
        with self.assertRaises(subprocess.CalledProcessError):
            api.revision(self.root, '--all')

    def test_duplicate_modules_fail(self):
        self.policy['runtime_modules'].append('domposable-ui')
        with self.assertRaisesRegex(ValueError, 'distinct'):
            api.expected_paths(self.policy)


if __name__ == '__main__':
    unittest.main()
