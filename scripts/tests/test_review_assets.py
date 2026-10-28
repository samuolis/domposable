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
    def test_review_requires_only_automatically_captured_platforms(self):
        self.assertEqual(module.REVIEW_PLATFORMS, {'android', 'desktop', 'dom', 'wasm'})
        self.assertEqual(module.REVIEW_ARTIFACTS, {'android-html-evidence'})

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

    def test_fresh_upload_uses_expected_url_without_rescanning_inventory(self):
        data = module.panel([Image.new('RGB', (1, 1), 'white')], ['PR'])
        with patch.object(module, 'evidence_bucket', return_value=({'tag_name': 'visual-evidence', 'id': 1}, [])), \
             patch.object(module, 'release_assets') as inventory, \
             patch.object(module.subprocess, 'run') as upload, \
             patch.object(module, 'urlopen', return_value=BytesIO(data)) as download:
            markdown = module.publish_images([('ios', 'shop', data, 'iOS shop')], 9, 1234)

        inventory.assert_not_called()
        upload.assert_called_once()
        self.assertNotIn('--clobber', upload.call_args.args[0])
        download.assert_called_once_with(
            f'https://github.com/{module.REPO}/releases/download/visual-evidence/'
            f'{module.asset_name(9, 1234, "ios", "shop", data)}',
            timeout=30,
        )
        self.assertIn(download.call_args.args[0], markdown)

    def test_published_asset_download_retries_not_found_then_succeeds(self):
        url = 'https://example.test/review.png'
        missing = module.HTTPError(url, 404, 'Not Found', None, None)
        with patch.object(module, 'urlopen', side_effect=[missing, BytesIO(b'png')]) as download, \
             patch.object(module.time, 'sleep') as sleep:
            self.assertEqual(module.download_published_asset(url), b'png')

        self.assertEqual(download.call_count, 2)
        sleep.assert_called_once_with(1)

    def test_published_asset_download_retries_transient_network_errors(self):
        url = 'https://example.test/review.png'
        errors = [module.HTTPError(url, 429, 'Rate limited', None, None),
                  module.HTTPError(url, 503, 'Unavailable', None, None),
                  module.URLError('temporary connection failure'), TimeoutError()]
        for error in errors:
            with self.subTest(error=type(error).__name__), \
                 patch.object(module, 'urlopen', side_effect=[error, BytesIO(b'png')]) as download, \
                 patch.object(module.time, 'sleep') as sleep:
                self.assertEqual(module.download_published_asset(url), b'png')
                self.assertEqual(download.call_count, 2)
                sleep.assert_called_once_with(1)

    def test_published_asset_download_raises_after_bounded_retries(self):
        url = 'https://example.test/review.png'
        missing = module.HTTPError(url, 404, 'Not Found', None, None)
        with patch.object(module, 'urlopen', side_effect=[missing, missing, missing]) as download, \
             patch.object(module.time, 'sleep') as sleep:
            with self.assertRaises(module.HTTPError): module.download_published_asset(url)

        self.assertEqual(download.call_count, 3)
        self.assertEqual([call.args[0] for call in sleep.call_args_list], [1, 3])

    def test_published_asset_download_does_not_retry_forbidden(self):
        url = 'https://example.test/review.png'
        forbidden = module.HTTPError(url, 403, 'Forbidden', None, None)
        with patch.object(module, 'urlopen', side_effect=forbidden) as download, \
             patch.object(module.time, 'sleep') as sleep:
            with self.assertRaises(module.HTTPError): module.download_published_asset(url)

        download.assert_called_once_with(url, timeout=30)
        sleep.assert_not_called()

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

    def test_unchanged_and_tolerated_noise_are_omitted(self):
        before = Image.new('RGB', (80, 80), 'white')
        self.assertIsNone(module.compare(before, before.copy()))
        noisy = before.copy()
        noisy.putpixel((40, 40), (243, 255, 255))
        self.assertIsNone(module.compare(before, noisy))
        with patch.object(module, 'evidence_bucket') as bucket:
            self.assertIn('No screenshot changes', module.publish_images([], 9, 1234))
            bucket.assert_not_called()

    def test_sparse_change_is_cropped_and_middle_panel_contains_only_changes(self):
        before = Image.new('RGB', (1000, 1000), 'black')
        after = before.copy()
        after.putpixel((500, 500), (0, 0, 255))
        image = module.png(module.compare(before, after))
        self.assertEqual(image.size, (243, 113))
        self.assertEqual(image.getpixel((97, 40)), (255, 255, 255))
        self.assertEqual(image.getpixel((121, 64)), (237, 0, 140))
        self.assertEqual(image.getpixel((124, 64)), (237, 0, 140))

    def test_dimension_changes_are_not_omitted(self):
        self.assertIsNotNone(module.compare(Image.new('RGB', (4, 4)), Image.new('RGB', (5, 4))))
