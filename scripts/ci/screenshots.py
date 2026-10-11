#!/usr/bin/env python3
"""Compare platform images with reviewed Git baselines; never bless them during PR CI."""
from __future__ import annotations
import argparse
import json
import os
from pathlib import Path
import shutil
import sys
from PIL import Image, ImageChops

ROOT = Path(__file__).resolve().parents[2]

def compare(actual: Path, baseline: Path, diff: Path) -> tuple[bool, str]:
    if not baseline.is_file(): return False, 'missing baseline'
    with Image.open(actual) as image: a = image.convert('RGB')
    with Image.open(baseline) as image: b = image.convert('RGB')
    if a.size != b.size: return False, f'size changed: {b.size} → {a.size}'
    delta = ImageChops.difference(a, b)
    # Ignore tiny channel rounding, never ignore moved controls or layout differences.
    mask = delta.convert('RGB').point(lambda x: 255 if x > 12 else 0).convert('L').point(lambda x: 255 if x else 0)
    changed = sum(1 for value in mask.getdata() if value)
    ratio = changed / (a.width * a.height)
    if ratio <= 0.0005: return True, f'{ratio:.3%} changed pixels'
    diff.parent.mkdir(parents=True, exist_ok=True)
    overlay = b.copy(); overlay.paste(Image.new('RGB', b.size, '#ff0080'), mask=mask); overlay.save(diff)
    return False, f'{ratio:.3%} changed pixels (limit 0.05%)'

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('platform', choices=json.loads((ROOT/'screenshots/manifest.json').read_text()))
    parser.add_argument('--record', action='store_true', help='Export candidates, not Git baselines')
    args = parser.parse_args()
    names = json.loads((ROOT/'screenshots/manifest.json').read_text())[args.platform]
    actual = ROOT/'build/screenshots'/args.platform
    report = ROOT/'build/visual-report'/args.platform
    report.mkdir(parents=True, exist_ok=True)
    failures = []
    rows = []
    for name in names:
        source = actual/f'{name}.png'
        if not source.is_file(): ok, message = False, 'missing capture'
        elif args.record: ok, message = True, 'candidate captured; review before committing'
        else:
            baseline = ROOT/'screenshots'/args.platform/f'{name}.png'
            if baseline.is_file(): shutil.copy2(baseline, report/f'{name}-expected.png')
            ok, message = compare(source, baseline, report/f'{name}-diff.png')
        if source.is_file(): shutil.copy2(source, report/f'{name}-actual.png')
        rows.append({'name':name, 'passed':ok, 'message':message})
        if not ok: failures.append(name)
    (report/'report.json').write_text(json.dumps(rows, indent=2)+'\n')
    summary = f'### {args.platform} visual checks\n\n| Capture | Result |\n| --- | --- |\n' + ''.join(f"| {row['name']} | {'PASS' if row['passed'] else 'FAIL'}: {row['message']} |\n" for row in rows)
    print(summary)
    if path := os.environ.get('GITHUB_STEP_SUMMARY'):
        with open(path,'a') as stream: stream.write(summary+'\n')
    if failures: sys.exit('Visual checks failed: '+', '.join(failures))

if __name__ == '__main__': main()
