"""Content-address browser entries and record the complete distribution's SHA-256."""
from __future__ import annotations
import hashlib
import json
from pathlib import Path
import re
import shutil

MANIFEST = 'web-assets.json'

def digest(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()

def inventory(folder: Path) -> dict[str, str]:
    return {path.relative_to(folder).as_posix(): digest(path.read_bytes())
            for path in sorted(folder.rglob('*')) if path.is_file() and path.name != MANIFEST}

def distribution_hash(files: dict[str, str]) -> str:
    return digest(json.dumps(files, sort_keys=True, separators=(',', ':')).encode())

def copy_hashed_bundle(source: Path, target: Path) -> dict:
    if source.resolve() == target.resolve():
        raise ValueError('Fingerprint a copy, never the compiler output')
    if target.exists(): shutil.rmtree(target)
    shutil.copytree(source, target)
    entry = target / 'demo.js'
    code = entry.read_bytes()
    # Source maps and license sidecars keep working under their own content hashes.
    for suffix in ['.map', '.LICENSE.txt']:
        sidecar = target / ('demo.js' + suffix)
        if sidecar.is_file():
            name = f'demo.{digest(sidecar.read_bytes())}.js{suffix}'
            sidecar.rename(target / name)
            code = code.replace(sidecar.name.encode(), name.encode())
    name = f'demo.{digest(code)}.js'
    (target / name).write_bytes(code)
    entry.unlink()
    page = target / 'index.html'
    html, count = re.subn(r'(<script\b[^>]*\bsrc=["\'])demo\.js(["\'])',
                          lambda match: match[1] + name + match[2], page.read_text())
    if count != 1: raise ValueError('Expected one browser entry script in index.html')
    page.write_text(html)
    files = inventory(target)
    manifest = {'schema': 1, 'entry': name, 'sha256': distribution_hash(files), 'files': files}
    (target / MANIFEST).write_text(json.dumps(manifest, sort_keys=True, indent=2) + '\n')
    return {key: manifest[key] for key in ['entry', 'sha256']}

def verify_bundle(folder: Path, expected: dict) -> None:
    manifest = json.loads((folder / MANIFEST).read_text())
    files = inventory(folder)
    if manifest.get('schema') != 1 or manifest.get('files') != files or manifest.get('sha256') != distribution_hash(files):
        raise ValueError('Browser distribution checksum mismatch')
    if {key: manifest.get(key) for key in ['entry', 'sha256']} != expected:
        raise ValueError('Browser distribution differs from release metadata')
    entry = manifest['entry']
    if entry != f'demo.{files.get(entry)}.js' or (folder / 'demo.js').exists():
        raise ValueError('Browser entry is not content-addressed')
