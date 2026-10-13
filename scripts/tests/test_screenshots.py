import importlib.util
from pathlib import Path
import tempfile
import unittest
from PIL import Image, ImageDraw

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
    def test_only_known_ipad_system_date_is_excluded(self):
        with tempfile.TemporaryDirectory() as directory:
            a, b, diff = [Path(directory)/f'{name}.png' for name in ['actual','baseline','diff']]
            for size in [(1488,2266),(2266,1488)]:
                baseline = Image.new('RGB',size,'white')
                baseline.save(b)
                for rectangle, ignored in [((150,16,339,47),True),((32,16,139,47),False),((150,56,339,87),False)]:
                    actual = baseline.copy()
                    ImageDraw.Draw(actual).rectangle(rectangle,fill='black')
                    actual.save(a)
                    self.assertEqual(module.compare(a,b,diff,'ios')[0],ignored)
                    self.assertFalse(module.compare(a,b,diff,'dom')[0])
            Image.new('RGB',(100,100),'white').save(b)
            Image.new('RGB',(100,100),'black').save(a)
            self.assertFalse(module.compare(a,b,diff,'ios')[0])
