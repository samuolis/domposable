#!/usr/bin/env python3
from pathlib import Path
import shutil

root = Path(__file__).resolve().parents[2]
output = root / "build/browser-preview"
output.mkdir(parents=True, exist_ok=True)
bundle = root / "demo/build/kotlin-webpack/js/developmentExecutable"
if not bundle.is_dir():
    raise SystemExit("Build :demo:jsBrowserDevelopmentWebpack first")
shutil.copytree(bundle, output, dirs_exist_ok=True)
shutil.copytree(root / "demo/build/processedResources/js/main", output, dirs_exist_ok=True)
