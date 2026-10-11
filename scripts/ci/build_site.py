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
subprocess.run([sys.executable, "-m", "mkdocs", "build", "--strict", "--site-dir", "build/site"], cwd=root, check=True)
site = root / "build/site"
api = root / "build/dokka/html"
wasm = root / "demo/build/dist/wasmJs/productionExecutable"
demo = root / "demo/build/dist/js/productionExecutable"
if not api.is_dir() or not demo.is_dir() or not wasm.is_dir(): raise SystemExit("Build Dokka and both browser distributions before assembling the site")
shutil.copytree(api, site / "api", dirs_exist_ok=True)
shutil.copytree(demo, site / "demo", dirs_exist_ok=True)
shutil.copytree(wasm, site / "wasm", dirs_exist_ok=True)

from web_routes import assemble_routes
assemble_routes(demo, site, "/domposable/routes/")

version, build = read_version(root / "version.properties")
commit = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=root, text=True).strip()
(site / "release.json").write_text(json.dumps({"version": str(version), "build": build, "commit": commit, "status": "preview" if build == 0 else "release"}) + "\n")
target = root / ".release/artifacts/website.zip"; target.parent.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(target, "w", zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(site.rglob("*")):
        if path.is_file(): archive.write(path, path.relative_to(site))
