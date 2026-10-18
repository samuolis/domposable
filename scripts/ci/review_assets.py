#!/usr/bin/env python3
"""Publish content-addressed CI PNG comparisons as GitHub release assets.

Requires gh authenticated for this repository. Prints Markdown to paste into Visual evidence.
"""
from __future__ import annotations
import argparse
import base64
import hashlib
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
ASSET_TAG = 'visual-evidence'
MAX_IMAGE = 20 * 1024 * 1024
Image.MAX_IMAGE_PIXELS = 20_000_000

def api(path, payload=None):
    command = ['gh', 'api', f'repos/{REPO}/{path}']
    if payload is not None: command += ['--method', 'POST', '--input', '-']
    return json.loads(subprocess.check_output(command, input=json.dumps(payload).encode() if payload is not None else None))

def png(data: bytes):
    if len(data) > MAX_IMAGE or not data.startswith(b'\x89PNG\r\n\x1a\n'): raise ValueError('Expected bounded PNG bytes')
    image = Image.open(BytesIO(data))
    if image.width * image.height > 20_000_000: raise ValueError('PNG dimensions exceed the review limit')
    image.load()
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

def asset_name(pr: int, run: int, platform: str, scenario: str, data: bytes) -> str:
    if pr <= 0 or run <= 0 or platform not in {'android', 'desktop', 'ios', 'dom', 'wasm'} or not re.fullmatch(r'[a-z0-9-]+', scenario):
        raise ValueError('Invalid review asset identity')
    return f'pr-{pr}-run-{run}-{platform}-{scenario}-{hashlib.sha256(data).hexdigest()}.png'

def validate_manifest(manifest):
    platforms = {'android', 'desktop', 'ios', 'dom', 'wasm'}
    if set(manifest) != platforms or any(
        not isinstance(names, list) or not 0 < len(names) <= 64 or len(set(names)) != len(names)
        for names in manifest.values()
    ):
        raise ValueError('Expected five bounded, unique platform screenshot inventories')

REVIEW_PLATFORMS = {'android', 'desktop', 'dom', 'wasm'}
REVIEW_ARTIFACTS = {'android-html-evidence'}

def release_assets(release_id):
    assets = []
    for page in range(1, 12):
        batch = api(f'releases/{release_id}/assets?per_page=100&page={page}')
        assets.extend(batch)
        if len(batch) < 100: return assets
    raise ValueError('Unexpected release asset inventory')

def evidence_bucket(count):
    # Each bucket holds up to GitHub's 1,000-asset limit. It is never marked latest.
    for index in range(1, 101):
        tag = ASSET_TAG if index == 1 else f'{ASSET_TAG}-{index}'
        result = subprocess.run(['gh', 'api', f'repos/{REPO}/releases/tags/{tag}'], capture_output=True)
        if result.returncode:
            if 'HTTP 404' not in result.stderr.decode():
                raise RuntimeError(result.stderr.decode())
            release = api('releases', {
                'tag_name': tag, 'target_commitish': 'main', 'name': f'Visual review assets {index}',
                'prerelease': True, 'make_latest': 'false',
                'body': 'Screenshot comparisons for pull request review. These are evidence assets, not a Domposable library version. Filenames include the PR, CI run and SHA-256 of the image. Reviewed golden inputs remain in main; full captures are retained in Actions artifacts.',
            })
            return release, []
        release = json.loads(result.stdout)
        assets = release_assets(release['id'])
        if not release.get('immutable') and len(assets) + count <= 1000:
            return release, assets
    raise ValueError('Visual evidence buckets are full')

def publish_images(files, pr: int, run: int):
    release, existing = evidence_bucket(len(files))
    by_name = {asset['name']: asset for asset in existing}
    lines = []
    with tempfile.TemporaryDirectory(prefix='domposable-review-assets-') as directory:
        for platform, scenario, data, label in files:
            png(data)
            name = asset_name(pr, run, platform, scenario, data)
            digest = 'sha256:' + hashlib.sha256(data).hexdigest()
            asset = by_name.get(name)
            if asset is None:
                path = Path(directory) / name
                path.write_bytes(data)
                # No --clobber: a previously published image is never overwritten.
                subprocess.run(['gh', 'release', 'upload', release['tag_name'], str(path), '--repo', REPO], check=True)
                by_name = {asset['name']: asset for asset in release_assets(release['id'])}
                asset = by_name[name]
            if asset.get('digest') not in (None, digest):
                raise ValueError('Published asset digest does not match its filename')
            url = asset['browser_download_url']
            expected = f'https://github.com/{REPO}/releases/download/{release["tag_name"]}/{name}'
            if url != expected: raise ValueError('Unexpected release asset URL')
            with urlopen(url, timeout=30) as response: downloaded = response.read(MAX_IMAGE + 1)
            if downloaded != data: raise ValueError('Published image bytes differ from reviewed evidence')
            png(downloaded)
            lines.append(f'![{label}]({url})')
    return '\n\n'.join(lines) + '\n'

def main():
    parser = argparse.ArgumentParser(); parser.add_argument('--run', required=True, type=int); parser.add_argument('--pr', required=True, type=int)
    args = parser.parse_args()
    pr = api(f'pulls/{args.pr}'); run = api(f'actions/runs/{args.run}')
    if pr['state'] != 'open' or pr['head']['repo']['full_name'] != REPO: raise SystemExit('Only open same-repository PRs are supported')
    if run['head_sha'] != pr['head']['sha'] or run['path'] != '.github/workflows/ci.yml' or run['status'] != 'completed': raise SystemExit('Use completed CI for the current PR head')
    artifacts = api(f'actions/runs/{args.run}/artifacts')['artifacts']
    platforms, names = REVIEW_PLATFORMS, REVIEW_ARTIFACTS
    selected = [a for a in artifacts if a['name'] in names and not a['expired']]
    if {a['name'] for a in selected} != names or any(a['size_in_bytes'] > 600 * 1024 * 1024 for a in selected):
        raise SystemExit('Expected bounded, available CI evidence artifacts for this validation scope')
    manifest_file = api(f'contents/screenshots/manifest.json?ref={run["head_sha"]}')
    manifest = json.loads(base64.b64decode(manifest_file['content']))
    validate_manifest(manifest)
    files = []
    with tempfile.TemporaryDirectory(prefix='domposable-visual-review-') as directory:
        for artifact in sorted(names):
            subprocess.run(['gh','run','download',str(args.run),'--repo',REPO,'--dir',str(Path(directory)/artifact),'--name',artifact],check=True)
        for platform, names in manifest.items():
            if platform not in {'android','desktop','ios','dom','wasm'}: raise SystemExit('Unknown screenshot platform')
            if platform not in platforms: continue
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
                files.append((platform, name, data, label))
    markdown = publish_images(files, args.pr, args.run)
    Path('build').mkdir(exist_ok=True); Path('build/visual-review.md').write_text(markdown)
    print(markdown)

if __name__ == '__main__': main()
