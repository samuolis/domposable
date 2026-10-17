#!/usr/bin/env python3
"""Export measured HTML/Wasm differences; independent goldens do not prove renderer parity."""
import json
import os
from pathlib import Path
from PIL import ImageChops
from review_assets import panel, png

ROOT = Path(__file__).resolve().parents[2]
THEMES = ['light', 'dark', 'brand-light', 'brand-dark', 'overrides-light', 'overrides-dark', 'expressive-light', 'expressive-dark']
FAMILIES = ['states', 'actions', 'selection', 'inputs', 'surfaces', 'navigation', 'feedback', 'gestures', 'scaffold']


def comparison(html, wasm):
    if html.size != wasm.size:
        raise ValueError('Both renderers must use the same viewport and pixel density')
    difference = ImageChops.difference(html, wasm)
    mask = difference.point(lambda value: 255 if value > 12 else 0).convert('L').point(lambda value: 255 if value else 0)
    changed = sum(bool(value) for value in mask.getdata())
    highlight = wasm.copy()
    highlight.paste('#ed008c', mask=mask)
    return panel([html, highlight, wasm], ['HTML', 'Pixel difference', 'Compose UI / Wasm']), changed / (html.width * html.height)


def capture_states(family):
    if family != 'states':
        return ['rest', 'end']
    return ['rest', 'hover', 'pressed', 'focus'] + [
        f'{control}-{state}' for control in ['switch', 'radio', 'slider'] for state in ['hover', 'pressed', 'focus']
    ]


def main():
    output = ROOT / 'build/renderer-report'
    output.mkdir(parents=True, exist_ok=True)
    rows = []
    for theme in THEMES:
        for family in FAMILIES:
            for width in [390, 1280]:
                name = f'{theme}-{family}-{width}'
                captures = ROOT / 'build/renderer-comparison' / name
                for state in capture_states(family):
                    suffix = '' if state == 'rest' else '-' + state
                    data, ratio = comparison(png((captures / f'html{suffix}.png').read_bytes()), png((captures / f'wasm{suffix}.png').read_bytes()))
                    image = f'{name}{suffix}.png'
                    (output / image).write_bytes(data)
                    rows.append({'theme': theme, 'family': family, 'width': width, 'state': state, 'changed_pixels': ratio, 'image': image})
    (output / 'report.json').write_text(json.dumps(rows, indent=2) + '\n')
    summary = '### HTML / Compose UI renderer differences\n\nThese measurements expose renderer differences; they are not a claim of visual parity or a replacement for regression gates.\n\n| Theme | Family | Width | Interaction | Changed pixels |\n| --- | --- | --- | --- | --- |\n'
    summary += ''.join(f"| {r['theme']} | {r['family']} | {r['width']} | {r['state']} | {r['changed_pixels']:.2%} |\n" for r in rows)
    (output / 'report.md').write_text(summary)
    if path := os.environ.get('GITHUB_STEP_SUMMARY'):
        with open(path, 'a') as stream:
            stream.write(summary)


if __name__ == '__main__':
    main()
