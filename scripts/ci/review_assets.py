#!/usr/bin/env python3
"""Publish CI PNG comparisons to an isolated immutable Git ref; never execute artifact code.

Requires gh authenticated for this repository. Prints Markdown to paste into Visual evidence.
"""
from __future__ import annotations
import argparse
import base64
from io import BytesIO
import json
from pathlib import Path
import re
import subprocess
import tempfile
from urllib.request import urlopen
from urllib.error import HTTPError
from PIL import Image, ImageChops, ImageDraw

REPO = 'samuolis/domposable'
MAX_IMAGE = 20 * 1024 * 1024
Image.MAX_IMAGE_PIXELS = 20_000_000

def api(path, payload=None):
    command = ['gh', 'api', f'repos/{REPO}/{path}']
    if payload is not None: command += ['--method', 'POST', '--input', '-']
    return json.loads(subprocess.check_output(command, input=json.dumps(payload).encode() if payload is not None else None))

def png(data: bytes):
    if len(data) > MAX_IMAGE or not data.startswith(b'\x89PNG\r\n\x1a\n'): raise ValueError('Expected bounded PNG bytes')
    image = Image.open(BytesIO(data)); image.load()
    return image.convert('RGB')

def panel(images, labels):
    scaled = []
    for im in images:
        width = min(600, im.width)
        scaled.append(im.resize((width, round(im.height * width / im.width))))
    result = Image.new('RGB', (sum(im.width for im in scaled) + 24 * (len(scaled) + 1), max(im.height for im in scaled) + 64), '#f4f0ff')
    draw = ImageDraw.Draw(result); x = 24
    for image, label in zip(scaled, labels):
        draw.text((x, 16), label, fill='#242137')
        result.paste(image, (x, 40)); x += image.width + 24
    out = BytesIO(); result.save(out, format='PNG'); return out.getvalue()

def compare(before, after):
    if before.size == after.size:
        diff = ImageChops.difference(before, after)
        # Match the regression gate: a difference in any channel counts.
        mask = diff.point(lambda v: 255 if v > 12 else 0).convert('L').point(lambda v: 255 if v else 0)
        highlight = after.copy(); highlight.paste(Image.new('RGB', after.size, '#ed008c'), mask=mask)
    else:
        highlight = Image.new('RGB', after.size, '#ed008c')
    return panel([before, highlight, after], ['Main', 'Pixel difference', 'PR'])

def main():
    parser = argparse.ArgumentParser(); parser.add_argument('--run', required=True, type=int); parser.add_argument('--pr', required=True, type=int)
    args = parser.parse_args()
    pr = api(f'pulls/{args.pr}'); run = api(f'actions/runs/{args.run}')
    if pr['state'] != 'open' or pr['head']['repo']['full_name'] != REPO: raise SystemExit('Only open same-repository PRs are supported')
    if run['head_sha'] != pr['head']['sha'] or run['path'] != '.github/workflows/ci.yml' or run['status'] != 'completed': raise SystemExit('Use completed CI for the current PR head')
    artifacts = api(f'actions/runs/{args.run}/artifacts')['artifacts']
    names = {'android-html-evidence','native-evidence'}
    selected = [a for a in artifacts if a['name'] in names and not a['expired']]
    if {a['name'] for a in selected} != names or any(a['size_in_bytes'] > 600 * 1024 * 1024 for a in selected):
        raise SystemExit('Expected two bounded, available CI evidence artifacts')
    manifest_file = api(f'contents/screenshots/manifest.json?ref={run["head_sha"]}')
    manifest = json.loads(base64.b64decode(manifest_file['content']))
    if sum(len(names) for names in manifest.values()) > 64: raise SystemExit('Unexpected screenshot inventory size')
    files = []
    with tempfile.TemporaryDirectory(prefix='domposable-visual-review-') as directory:
        subprocess.run(['gh','run','download',str(args.run),'--repo',REPO,'--dir',directory,'--name','android-html-evidence','--name','native-evidence'],check=True)
        for platform, names in manifest.items():
            if platform not in {'android','desktop','ios','dom','wasm'}: raise SystemExit('Unknown screenshot platform')
            for name in names:
                if not re.fullmatch(r'[a-z0-9-]+', name): raise SystemExit('Invalid scenario name')
                candidates = list(Path(directory).glob(f'*/build/screenshots/{platform}/{name}.png'))
                if len(candidates) != 1: raise SystemExit(f'Missing/ambiguous capture: {platform}/{name}')
                after = png(candidates[0].read_bytes())
                baseline_path = f'screenshots/{platform}/{name}.png'
                try:
                    url = f'https://raw.githubusercontent.com/{REPO}/{pr["base"]["sha"]}/{baseline_path}'
                    with urlopen(url, timeout=30) as response: before = png(response.read(MAX_IMAGE+1))
                except HTTPError as error:
                    if error.code != 404: raise
                    before = None
                if before is not None:
                    data = compare(before, after); label = f'{platform} {name}: Main | Pixel difference | PR'
                else:
                    data = panel([after], [f'Added: {platform} {name}']); label = f'{platform} {name}: added preview'
                blob = api('git/blobs', {'content':base64.b64encode(data).decode(), 'encoding':'base64'})
                files.append((f'previews/{platform}/{name}.png', blob['sha'], label))
    tree = api('git/trees', {'tree':[{'path':path,'mode':'100644','type':'blob','sha':sha} for path,sha,_ in files]})
    commit = api('git/commits', {'message':f'Visual review for PR #{args.pr}, CI {args.run}, head {run["head_sha"]}', 'tree':tree['sha'], 'parents':[]})
    api('git/refs', {'ref':f'refs/heads/visual-review/pr-{args.pr}/run-{args.run}', 'sha':commit['sha']})
    lines = []
    for path, _, label in files:
        url = f'https://raw.githubusercontent.com/{REPO}/{commit["sha"]}/{path}'
        with urlopen(url,timeout=30) as response: png(response.read(MAX_IMAGE+1))
        lines.append(f'![{label}]({url})')
    markdown = '\n\n'.join(lines) + '\n'
    Path('build').mkdir(exist_ok=True); Path('build/visual-review.md').write_text(markdown)
    print(markdown)

if __name__ == '__main__': main()
