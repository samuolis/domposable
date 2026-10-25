#!/usr/bin/env python3
"""Assemble one website containing guides, generated API and the HTML demo."""
import json
from pathlib import Path
import shutil
import subprocess
import sys
import zipfile
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from version import read_version
root = Path(__file__).resolve().parents[2]
subprocess.run([sys.executable, "scripts/ci/api_reference.py", "--check"], cwd=root, check=True)
subprocess.run([sys.executable, "-m", "mkdocs", "build", "--strict", "--site-dir", "build/site"], cwd=root, check=True)
site = root / "build/site"
api = root / "build/dokka/html"
wasm = root / "demo/build/dist/wasmJs/productionExecutable"
demo = root / "demo/build/dist/js/productionExecutable"
if not api.is_dir() or not demo.is_dir() or not wasm.is_dir(): raise SystemExit("Build Dokka and both browser distributions before assembling the site")
shutil.copytree(api, site / "api", dirs_exist_ok=True)
from web_assets import copy_hashed_bundle, distribution_hash, inventory, verify_bundle
web = {'html': copy_hashed_bundle(demo, site / 'demo'), 'wasm': copy_hashed_bundle(wasm, site / 'wasm')}
verify_bundle(site / 'demo', web['html'])
verify_bundle(site / 'wasm', web['wasm'])
api_files = inventory(site / 'api', exclude_manifest=False)
api = {'sha256': distribution_hash(api_files), 'files': api_files}

from web_routes import assemble_routes
assemble_routes(site / 'demo', site, "/domposable/routes/")

version, build = read_version(root / "version.properties")
commit = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=root, text=True).strip()
(site / "release.json").write_text(json.dumps({"version": str(version), "build": build, "commit": commit, "status": "preview" if build == 0 else "release", "api": api, "web": web}) + "\n")
target = root / ".release/artifacts/website.zip"; target.parent.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(target, "w", zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(site.rglob("*")):
        if path.is_file(): archive.write(path, path.relative_to(site))
