import importlib.util
from pathlib import Path
import tempfile
import unittest

SPEC = importlib.util.spec_from_file_location(
    "coverage_report", Path(__file__).parents[1] / "ci" / "coverage_report.py"
)
coverage_report = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(coverage_report)


class CoverageReportTest(unittest.TestCase):
    def read(self, content):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "coverage.xml"
            path.write_text(content)
            return coverage_report.read_report(path)

    def test_uses_aggregate_counters_instead_of_double_counting_classes(self):
        report = self.read('''<report>
          <package name="example/core"><class name="example/core/Thing">
            <counter type="LINE" covered="6" missed="2"/>
          </class><counter type="LINE" covered="6" missed="2"/></package>
          <counter type="LINE" covered="6" missed="2"/>
          <counter type="BRANCH" covered="1" missed="3"/>
        </report>''')
        self.assertEqual(75.0, report["line"]["percent"])
        self.assertEqual(25.0, report["branch"]["percent"])
        self.assertEqual("example.core", report["packages"][0]["name"])
        self.assertIn("JVM and Android", coverage_report.markdown(report))

    def test_absent_or_empty_coverage_cannot_be_reported_as_success(self):
        for xml in (
            "<report/>",
            '<report><counter type="LINE" covered="0" missed="0"/></report>',
            '<report><counter type="LINE" covered="-1" missed="2"/></report>',
            "<testsuite/>",
        ):
            with self.subTest(xml=xml), self.assertRaises(ValueError):
                self.read(xml)

    def test_no_branches_is_not_claimed_as_full_branch_coverage(self):
        report = self.read('<report><counter type="LINE" covered="1" missed="0"/></report>')
        self.assertIsNone(report["branch"]["percent"])
        self.assertIn("n/a", coverage_report.markdown(report))

    def test_floors_fail_for_regressions_and_missing_required_packages(self):
        report = self.read('<report><counter type="LINE" covered="3" missed="1"/></report>')
        coverage_report.verify(report, {"total": {"line": 70}, "packages": {}})
        with self.assertRaisesRegex(ValueError, "below 80"):
            coverage_report.verify(report, {"total": {"line": 80}, "packages": {}})
        with self.assertRaisesRegex(ValueError, "package is missing"):
            coverage_report.verify(report, {"total": {"line": 70}, "packages": {"example": {"line": 70}}})
        with self.assertRaisesRegex(ValueError, "branch coverage None"):
            coverage_report.verify(report, {"total": {"branch": 35}, "packages": {}})



if __name__ == "__main__":
    unittest.main()
