import importlib.util
from pathlib import Path
import tempfile
import unittest

SPEC = importlib.util.spec_from_file_location(
    "serve_kobweb", Path(__file__).parents[1] / "ci/serve_kobweb.py"
)
host = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(host)


class KobwebHostTest(unittest.TestCase):
    def test_parent_page_takes_precedence_over_dynamic_route_directory(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "contacts").mkdir()
            (root / "contacts.html").write_text("Contacts")
            (root / "contacts/alex.html").write_text("Alex")
            handler = host.ExportHandler.__new__(host.ExportHandler)
            handler.directory = directory
            for route in ("/contacts", "/contacts/", "/contacts?search=Alex"):
                self.assertEqual(Path(handler.translate_path(route)), root / "contacts.html")
            self.assertEqual(Path(handler.translate_path("/contacts/alex")), root / "contacts/alex.html")
            self.assertFalse(Path(handler.translate_path("/contacts/missing")).exists())

    def test_regular_assets_and_path_traversal_keep_standard_host_boundaries(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "site.js").write_text("script")
            handler = host.ExportHandler.__new__(host.ExportHandler)
            handler.directory = directory
            self.assertEqual(Path(handler.translate_path("/site.js")), root / "site.js")
            self.assertTrue(Path(handler.translate_path("/../../outside")).is_relative_to(root))
