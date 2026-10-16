import importlib.util
from io import BytesIO
from pathlib import Path
import unittest
import warnings
import hashlib
from unittest.mock import patch
from PIL import Image

spec = importlib.util.spec_from_file_location('review_assets', Path(__file__).parents[1]/'ci/review_assets.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

class ReviewEvidenceTest(unittest.TestCase):
    def test_catalog_manifest_is_bounded_per_platform(self):
        manifest = {platform: [f'scenario-{i}' for i in range(24)] for platform in ('android', 'desktop', 'ios', 'dom', 'wasm')}
        module.validate_manifest(manifest)
        for names in ([], ['duplicate', 'duplicate'], [f'scenario-{i}' for i in range(65)]):
            with self.assertRaises(ValueError):
                module.validate_manifest(dict(manifest, dom=names))
        with self.assertRaises(ValueError):
            module.validate_manifest({'dom': ['only-one-platform']})

    def test_asset_identity_includes_full_content_digest(self):
        name = module.asset_name(9, 1234, 'ios', 'adaptive', b'image')
        self.assertEqual(name, 'pr-9-run-1234-ios-adaptive-' + hashlib.sha256(b'image').hexdigest() + '.png')
        with self.assertRaises(ValueError): module.asset_name(9, 1234, 'ios', '../unsafe', b'image')

    def test_existing_asset_is_verified_without_clobbering(self):
        data = module.panel([Image.new('RGB', (1, 1), 'white')], ['PR'])
        name = module.asset_name(9, 1234, 'ios', 'shop', data)
        url = f'https://github.com/{module.REPO}/releases/download/visual-evidence/{name}'
        asset = {'name': name, 'browser_download_url': url, 'digest': 'sha256:' + hashlib.sha256(data).hexdigest()}
        with patch.object(module, 'evidence_bucket', return_value=({'tag_name': 'visual-evidence', 'id': 1}, [asset])), patch.object(module, 'urlopen', return_value=BytesIO(data)), patch.object(module.subprocess, 'run') as upload:
            self.assertIn(url, module.publish_images([('ios', 'shop', data, 'iOS shop')], 9, 1234))
            upload.assert_not_called()
        with patch.object(module, 'evidence_bucket', return_value=({'tag_name': 'visual-evidence', 'id': 1}, [asset])), patch.object(module, 'urlopen', return_value=BytesIO(b'wrong image')):
            with self.assertRaises(ValueError): module.publish_images([('ios', 'shop', data, 'iOS shop')], 9, 1234)

    def test_rejects_non_png_and_excessive_pixel_dimensions(self):
        with self.assertRaises(ValueError):
            module.png(b'<html>artifact content</html>')
        data = BytesIO()
        Image.new('1', (4473, 4473)).save(data, format='PNG')
        with warnings.catch_warnings():
            warnings.simplefilter('ignore', Image.DecompressionBombWarning)
            with self.assertRaises(ValueError):
                module.png(data.getvalue())

    def test_difference_in_one_channel_is_visible_in_review(self):
        before = Image.new('RGB', (1, 1), 'white')
        after = Image.new('RGB', (1, 1), (255, 255, 235))
        image = module.png(module.compare(before, after))
        # The middle panel marks a blue-only change beyond the gate's tolerance.
        self.assertEqual(image.getpixel((49, 40)), (237, 0, 140))
