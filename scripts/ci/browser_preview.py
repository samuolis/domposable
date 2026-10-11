#!/usr/bin/env python3
from pathlib import Path
import shutil
import subprocess
import sys

root = Path(__file__).resolve().parents[2]
output = root / "build/browser-preview"
shutil.rmtree(output, ignore_errors=True)
output.mkdir(parents=True, exist_ok=True)
subprocess.run([sys.executable, '-m', 'mkdocs', 'build', '--strict', '--site-dir', str(output)], cwd=root, check=True)
bundle = root / "demo/build/kotlin-webpack/js/developmentExecutable"
if not bundle.is_dir():
    raise SystemExit("Build :demo:jsBrowserDevelopmentWebpack first")
shutil.copytree(bundle, output, dirs_exist_ok=True)
shutil.copytree(root / "demo/build/processedResources/js/main", output, dirs_exist_ok=True)
shutil.copytree(bundle, output / "demo", dirs_exist_ok=True)
shutil.copytree(root / "demo/build/processedResources/js/main", output / "demo", dirs_exist_ok=True)

wasm = root / "demo/build/kotlin-webpack/wasmJs/developmentExecutable"
if not wasm.is_dir(): raise SystemExit("Build :demo:wasmJsBrowserDevelopmentWebpack first")
shutil.copytree(wasm, output / "wasm", dirs_exist_ok=True)
shutil.copytree(root / "demo/build/processedResources/wasmJs/main", output / "wasm", dirs_exist_ok=True)

from web_routes import assemble_routes
route_source = root / "build/routes-source"
shutil.rmtree(route_source, ignore_errors=True)
shutil.copytree(bundle, route_source)
shutil.copytree(root / "demo/build/processedResources/js/main", route_source, dirs_exist_ok=True)
assemble_routes(route_source, output, "/routes/")
shutil.copytree(root / "demo/build/processedResources/js/main/assets", output / "routes/assets", dirs_exist_ok=True)
