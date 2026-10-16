#!/usr/bin/env python3
"""Wait for publication visibility, then compile real consumers against Central."""
import subprocess
import time
import urllib.error
import urllib.request
from pathlib import Path
import sys
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from version import read_version
root = Path(__file__).resolve().parents[2]
version, _ = read_version(root / 'version.properties')
url = f'https://repo.maven.apache.org/maven2/io/github/samuolis/domposable/domposable-ui/{version}/domposable-ui-{version}.pom'
deadline = time.monotonic() + 1800
while True:
    try:
        with urllib.request.urlopen(url, timeout=30) as response:
            if response.status == 200: break
    except urllib.error.URLError: pass
    if time.monotonic() >= deadline: raise SystemExit('Central propagation timed out; resume this release')
    time.sleep(20)
subprocess.run(['./gradlew', ':consumer-fixture:verifyStableComposeBaseline', ':consumer-fixture:compileKotlinJvm', ':consumer-fixture:compileKotlinJs', ':consumer-fixture:compileKotlinWasmJs', ':consumer-fixture:compileAndroidMain', ':consumer-fixture:compileKotlinIosArm64', ':consumer-fixture:compileKotlinIosSimulatorArm64', ':custom-provider-fixture:compileKotlinJvm', ':custom-provider-fixture:compileKotlinJs', ':custom-provider-fixture:compileKotlinWasmJs', ':custom-provider-fixture:compileAndroidMain', ':custom-provider-fixture:compileKotlinIosArm64', ':custom-provider-fixture:compileKotlinIosSimulatorArm64', '-Pdomposable.consumer.repository=https://repo.maven.apache.org/maven2', '--refresh-dependencies', '--rerun-tasks', '--console=plain'], cwd=root, check=True)
