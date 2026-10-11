import importlib.util
from io import BytesIO
from pathlib import Path
import unittest
import warnings
from PIL import Image

spec = importlib.util.spec_from_file_location('review_assets', Path(__file__).parents[1]/'ci/review_assets.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

class ReviewEvidenceTest(unittest.TestCase):
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
