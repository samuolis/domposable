import importlib.util
from pathlib import Path
import tempfile
import unittest
from PIL import Image

spec = importlib.util.spec_from_file_location('screenshots', Path(__file__).parents[1]/'ci/screenshots.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

class VisualGateTest(unittest.TestCase):
    def test_missing_or_changed_size_never_passes(self):
        with tempfile.TemporaryDirectory() as directory:
            a, b, diff = [Path(directory)/f'{name}.png' for name in ['actual','baseline','diff']]
            Image.new('RGB',(50,50),'white').save(a)
            self.assertFalse(module.compare(a,b,diff)[0])
            Image.new('RGB',(51,50),'white').save(b)
            self.assertFalse(module.compare(a,b,diff)[0])
    def test_layout_change_produces_diff_and_rounding_is_tolerated(self):
        with tempfile.TemporaryDirectory() as directory:
            a, b, diff = [Path(directory)/f'{name}.png' for name in ['actual','baseline','diff']]
            Image.new('RGB',(100,100),'white').save(b)
            Image.new('RGB',(100,100),(250,250,250)).save(a)
            self.assertTrue(module.compare(a,b,diff)[0])
            Image.new('RGB',(100,100),'black').save(a)
            self.assertFalse(module.compare(a,b,diff)[0])
            self.assertTrue(diff.is_file())
