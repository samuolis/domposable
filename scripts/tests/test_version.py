import sys
import json
import subprocess
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from version import Version, read_checkpoint_version, read_version, write_version

class VersionTests(unittest.TestCase):
    def test_first_minor_release_from_unpublished_bootstrap(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "version.properties"
            path.write_text("VERSION=0.0.0\nBUILD_NUMBER=0\n")
            output = subprocess.check_output([sys.executable, str(Path(__file__).resolve().parents[1] / "version.py"), "--file", str(path), "--bump", "minor", "--write"], text=True)
            self.assertEqual(json.loads(output), {"version": "0.1.0", "build": 100, "marketing": "0.1.0"})
            self.assertEqual(read_version(path), (Version.parse("0.1.0"), 100))
            self.assertNotIn("BUILD_NUMBER", path.read_text())

    def test_numeric_order_and_semver_bumps(self):
        self.assertGreater(Version.parse("1.10.0").order, Version.parse("1.9.99").order)
        self.assertEqual(str(Version.parse("1.2.3").bump("patch")), "1.2.4")
        self.assertEqual(str(Version.parse("1.2.3").bump("minor")), "1.3.0")
        self.assertEqual(str(Version.parse("1.2.3").bump("major")), "2.0.0")
        with self.assertRaises(ValueError): Version.parse("1.2.3").bump("prerelease")

    def test_version_only_file_and_strict_properties(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "version.properties"
            write_version(path, Version.parse("0.1.0"))
            self.assertEqual(read_version(path), (Version.parse("0.1.0"), 100))
            path.write_text("VERSION=0.1.0\nVERSION=0.2.0\nBUILD_NUMBER=1\n")
            with self.assertRaises(ValueError): read_version(path)
            path.write_text("VERSION=0.1.0\nOTHER=1\n")
            with self.assertRaises(ValueError): read_version(path)

    def test_android_codes_increase_across_patch_minor_and_major_boundaries(self):
        versions = ["0.0.1", "0.3.0", "0.4.0", "0.4.1", "0.4.99", "0.5.0", "0.999.99", "1.0.0", "21000.0.0"]
        codes = [Version.parse(value).android_code for value in versions]
        self.assertEqual(codes, [1, 300, 400, 401, 499, 500, 99999, 100000, 2100000000])
        self.assertTrue(all(a < b for a, b in zip(codes, codes[1:])))

    def test_overflow_and_colliding_components_are_rejected(self):
        for value in ["0.1000.0", "0.1.100", "21000.0.1", "21001.0.0"]:
            with self.subTest(value=value), self.assertRaises(ValueError):
                Version.parse(value).android_code

    def test_legacy_counter_cannot_override_new_builds_but_checkpoints_recover_it(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "version.properties"
            path.write_text("VERSION=0.3.0\nBUILD_NUMBER=4\n")
            self.assertEqual(read_version(path), (Version.parse("0.3.0"), 300))
            self.assertEqual(read_checkpoint_version(path), (Version.parse("0.3.0"), 4))
            with self.assertRaisesRegex(ValueError, "VERSION only"):
                read_version(path, require_version_only=True)
            write_version(path, Version.parse("0.4.0"))
            self.assertEqual(read_checkpoint_version(path), (Version.parse("0.4.0"), 400))
            path.write_text("VERSION=0.3.0\nBUILD_NUMBER=-1\n")
            with self.assertRaises(ValueError): read_version(path)

    def test_bootstrap_is_not_written_as_a_published_release(self):
        with tempfile.TemporaryDirectory() as folder:
            with self.assertRaises(ValueError):
                write_version(Path(folder) / "version.properties", Version.parse("0.0.0"))

    def test_rejects_invalid_versions(self):
        for value in ["01.2.3", "1.2", "1.2.3;echo", "1.2.3-alpha", "1.2.3-alpha01", "1.2.3+build1"]:
            with self.subTest(value=value), self.assertRaises(ValueError): Version.parse(value)

if __name__ == "__main__": unittest.main()
