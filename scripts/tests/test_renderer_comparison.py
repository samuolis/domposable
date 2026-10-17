import sys
import unittest
from pathlib import Path
from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'ci'))
from renderer_comparison import comparison


class RendererComparisonTest(unittest.TestCase):
    def test_exact_pixels_have_no_difference(self):
        data, ratio = comparison(Image.new('RGB', (20, 20), 'white'), Image.new('RGB', (20, 20), 'white'))
        self.assertTrue(data.startswith(b'\x89PNG'))
        self.assertEqual(0, ratio)

    def test_changed_control_is_reported(self):
        html = Image.new('RGB', (20, 20), 'white')
        wasm = html.copy()
        wasm.putpixel((10, 10), (0, 0, 0))
        _, ratio = comparison(html, wasm)
        self.assertEqual(1 / 400, ratio)

    def test_mismatched_viewports_are_rejected(self):
        with self.assertRaises(ValueError):
            comparison(Image.new('RGB', (20, 20)), Image.new('RGB', (21, 20)))
