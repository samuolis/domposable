import tempfile
import unittest
from pathlib import Path
from scripts.ci.check_minimal_bundle import check_bundle


class MinimalBundleTest(unittest.TestCase):
    def test_requires_output_and_counts_all_chunks_but_not_source_maps(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            with self.assertRaisesRegex(ValueError, 'No JavaScript'):
                check_bundle(root)
            (root / 'main.js').write_bytes(bytes(range(80)))
            (root / 'main.js.map').write_bytes(b'unpublished map' * 100)
            chunks = root / 'chunks'
            chunks.mkdir()
            (chunks / 'feature.mjs').write_bytes(bytes(reversed(range(80))))
            report = check_bundle(root)
            self.assertEqual(report['files'], 2)
            self.assertEqual(report['bytes'], 160)
            with self.assertRaisesRegex(ValueError, 'over the 180-byte budget'):
                check_bundle(root, limit=180)
