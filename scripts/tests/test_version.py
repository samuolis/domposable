import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from version import Version, read_version, write_version

class VersionTests(unittest.TestCase):
    def test_bootstrap_and_channel_progression(self):
        initial = Version.parse("0.1.0-alpha00")
        self.assertEqual(str(initial.bump("prerelease")), "0.1.0-alpha01")
        self.assertEqual(str(initial.bump("prerelease", "beta")), "0.1.0-beta01")
        self.assertEqual(str(Version.parse("0.1.0-rc02").bump("stable")), "0.1.0")
        with self.assertRaises(ValueError): Version.parse("0.1.0-beta01").bump("prerelease", "alpha")

    def test_numeric_sequence_and_stable_bumps(self):
        self.assertGreater(Version.parse("0.1.0-alpha100").order, Version.parse("0.1.0-alpha99").order)
        self.assertEqual(str(Version.parse("1.2.3").bump("minor")), "1.3.0")
        self.assertEqual(str(Version.parse("1.2.3").bump("prerelease")), "1.2.4-alpha01")

    def test_strict_file_and_monotonic_build(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "version.properties"
            write_version(path, Version.parse("0.1.0-alpha01"), 1)
            self.assertEqual(read_version(path), (Version.parse("0.1.0-alpha01"), 1))
            path.write_text("VERSION=0.1.0\nVERSION=0.2.0\nBUILD_NUMBER=1\n")
            with self.assertRaises(ValueError): read_version(path)

    def test_rejects_invalid_versions(self):
        for value in ["01.2.3", "1.2", "1.2.3;echo", "1.2.3-alpha", "1.2.3-alpha-1"]:
            with self.subTest(value=value), self.assertRaises(ValueError): Version.parse(value)

if __name__ == "__main__": unittest.main()
