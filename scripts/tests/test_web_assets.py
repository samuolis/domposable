import json
from pathlib import Path
import sys
import tempfile
import unittest
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'ci'))
from web_assets import copy_hashed_bundle, digest, inventory, verify_bundle

class WebAssetsTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.source = self.root / 'compiler'; self.source.mkdir()
        (self.source / 'index.html').write_text('<html><head></head><body><script src="demo.js"></script></body></html>')
        (self.source / 'demo.js').write_text('/* demo.js.LICENSE.txt */\nfetch("1234.wasm");\n//# sourceMappingURL=demo.js.map')
        (self.source / 'demo.js.map').write_text('{"version":3,"sources":[]}')
        (self.source / 'demo.js.LICENSE.txt').write_text('license')
        (self.source / '1234.wasm').write_bytes(b'wasm binary')
    def tearDown(self): self.temp.cleanup()
    def test_inventory_can_include_a_manifest_named_file_outside_browser_bundles(self):
        path = self.source / 'web-assets.json'
        path.write_text('ordinary API content')
        self.assertNotIn('web-assets.json', inventory(self.source))
        self.assertIn('web-assets.json', inventory(self.source, exclude_manifest=False))
    def test_runtime_sidecars_and_html_use_content_hashes_without_changing_compiler_output(self):
        target = self.root / 'site'
        metadata = copy_hashed_bundle(self.source, target)
        verify_bundle(target, metadata)
        code = (target / metadata['entry']).read_text()
        self.assertIn(metadata['entry'], (target / 'index.html').read_text())
        self.assertIn('1234.wasm', code)
        for suffix in ['.map', '.LICENSE.txt']:
            expected = f'demo.{digest((self.source / ("demo.js" + suffix)).read_bytes())}.js{suffix}'
            self.assertIn(expected, code)
            self.assertTrue((target / expected).is_file())
        self.assertTrue((self.source / 'demo.js').exists())
    def test_reproducible_hash_changes_when_a_resource_changes(self):
        first = copy_hashed_bundle(self.source, self.root / 'one')
        self.assertEqual(first, copy_hashed_bundle(self.source, self.root / 'two'))
        (self.source / '1234.wasm').write_bytes(b'changed binary')
        self.assertNotEqual(first['sha256'], copy_hashed_bundle(self.source, self.root / 'three')['sha256'])
    def test_missing_or_corrupt_assets_fail_verification(self):
        target = self.root / 'site'
        metadata = copy_hashed_bundle(self.source, target)
        (target / '1234.wasm').unlink()
        with self.assertRaisesRegex(ValueError, 'checksum'): verify_bundle(target, metadata)
    def test_release_hash_cannot_describe_another_distribution(self):
        target = self.root / 'site'
        metadata = copy_hashed_bundle(self.source, target)
        with self.assertRaisesRegex(ValueError, 'release metadata'):
            verify_bundle(target, dict(metadata, sha256='0' * 64))
    def test_copied_nested_routes_keep_the_hashed_entry(self):
        from web_routes import assemble_routes
        output = self.root / 'site'; output.mkdir()
        metadata = copy_hashed_bundle(self.source, output / 'demo')
        assemble_routes(output / 'demo', output, '/domposable/routes/')
        verify_bundle(output / 'demo', metadata)
        self.assertFalse((output / 'routes/web-assets.json').exists())
        self.assertFalse((output / 'shop/web-assets.json').exists())
        for path in ['routes/tasks/welcome/index.html', 'shop/products/canvas-weekender/index.html']:
            page = (output / path).read_text()
            self.assertIn(metadata['entry'], page)
            self.assertNotIn('src="demo.js"', page)
