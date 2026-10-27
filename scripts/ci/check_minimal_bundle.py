#!/usr/bin/env python3
"""Keep the packaged button-and-text consumer below its compressed JS budget."""
import gzip
import json
from pathlib import Path


def check_bundle(directory, limit=300_000):
    files = sorted([*directory.rglob('*.js'), *directory.rglob('*.mjs')])
    if not files:
        raise ValueError(f'No JavaScript output in {directory}')
    raw = [path.read_bytes() for path in files]
    compressed = sum(len(gzip.compress(data, compresslevel=6, mtime=0)) for data in raw)
    if compressed > limit:
        raise ValueError(f'Minimal consumer JavaScript is {compressed:,} gzip bytes, over the {limit:,}-byte budget')
    return {'files': len(files), 'bytes': sum(map(len, raw)), 'gzip_bytes': compressed}


if __name__ == '__main__':
    root = Path(__file__).resolve().parents[2]
    # Count emitted executable code, not unused resources copied from transitive dependencies.
    print(json.dumps(check_bundle(root / 'consumer-fixture/build/kotlin-webpack/js/productionExecutable')))
