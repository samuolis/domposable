#!/usr/bin/env python3
"""Refresh guide illustrations from reviewed, checked-in screenshot inputs."""
from pathlib import Path
import argparse
from review_assets import panel, png

root = Path(__file__).resolve().parents[2]
parser = argparse.ArgumentParser(); parser.add_argument('--source', type=Path, default=root/'screenshots')
source = parser.parse_args().source
target = root/'docs/assets/examples'; target.mkdir(parents=True, exist_ok=True)
for name, inputs in {
    'shop-web': [('dom/shop-phone.png','HTML · phone'),('dom/shop-wide.png','HTML · desktop')],
    'shop-native': [('android/shop.png','Android'),('ios/shop.png','iOS · tablet')],
    'components': [('android/catalog.png','Android components'),('dom/catalog-wide.png','Semantic HTML components')],
    'selection-controls': [('android/extended-components.png','Android · selection and feedback'),('dom/extended-components.png','Semantic HTML · same feature')],
    'material-selection': [('android/material-selection.png','Android · Material selection'),('dom/material-selection.png','Semantic HTML · same feature')],
    'material-feedback': [('android/material-feedback.png','Android · expressive feedback'),('dom/material-feedback.png','Semantic HTML · same feature')],
}.items():
    images = [png((source/path).read_bytes()) for path,_ in inputs]
    (target/f'{name}.png').write_bytes(panel(images,[label for _,label in inputs]))
