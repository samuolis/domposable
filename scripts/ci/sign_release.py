#!/usr/bin/env python3
"""Build and verify the signed Android demo download with dedicated CI secrets."""
from __future__ import annotations
import base64
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from version import read_version

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / ".release/artifacts"
REQUIRED = ['ANDROID_KEYSTORE_BASE64', 'ANDROID_KEYSTORE_PASSWORD', 'ANDROID_KEY_ALIAS', 'ANDROID_KEY_PASSWORD', 'ANDROID_CERT_SHA256']

def run(*args, capture=False, env=None):
    result = subprocess.run(args, cwd=ROOT, env=env, stdout=subprocess.PIPE if capture else None, stderr=subprocess.PIPE if capture else None)
    if result.returncode: raise RuntimeError(f"{args[0]} failed; exit {result.returncode}")
    return result.stdout if capture else b""

def verify_apk(signer, analyzer, apk, version, build_number):
    verification = run(str(signer), 'verify', '--verbose', '--print-certs', str(apk), capture=True).decode()
    expected = os.environ['ANDROID_CERT_SHA256'].replace(':', '').lower()
    digests = [line.split(':', 1)[1].strip() for line in verification.lower().splitlines() if 'certificate sha-256 digest:' in line]
    if expected not in digests: raise RuntimeError("APK certificate fingerprint differs")
    if run(str(analyzer), 'manifest', 'version-name', str(apk), capture=True).decode().strip() != str(version): raise RuntimeError("APK version name differs")
    if run(str(analyzer), 'manifest', 'version-code', str(apk), capture=True).decode().strip() != str(build_number): raise RuntimeError("APK build number differs")

def build():
    missing = [key for key in REQUIRED if not os.environ.get(key)]
    if missing: raise RuntimeError("Configure dedicated release secrets: " + ", ".join(missing))
    ASSETS.mkdir(parents=True, exist_ok=True)
    version, build_number = read_version(ROOT / 'version.properties')
    with tempfile.TemporaryDirectory(prefix='domposable-signing-') as temporary:
        keystore = Path(temporary) / 'android.keystore'
        keystore.write_bytes(base64.b64decode(os.environ['ANDROID_KEYSTORE_BASE64'], validate=True))
        keystore.chmod(0o600)
        env = os.environ.copy(); env['ANDROID_KEYSTORE_PATH'] = str(keystore)
        run('./gradlew', ':androidApp:assembleRelease', '--console=plain', env=env)
        apk = ROOT / 'androidApp/build/outputs/apk/release/androidApp-release.apk'
        sdk = Path(os.environ.get('ANDROID_HOME', os.environ.get('ANDROID_SDK_ROOT', '')))
        signer = sorted((sdk / 'build-tools').glob('*/apksigner'), key=lambda p: tuple(int(n) for n in p.parent.name.split('.') if n.isdigit()))[-1]
        analyzer = sdk / 'cmdline-tools/latest/bin/apkanalyzer'
        if not analyzer.is_file(): analyzer = next((sdk / 'cmdline-tools').glob('*/bin/apkanalyzer'))
        verify_apk(signer, analyzer, apk, version, build_number)
        shutil.copy2(apk, ASSETS / f'domposable-{version}.apk')

if __name__ == '__main__':
    try: build()
    except (RuntimeError, KeyError, ValueError) as error: raise SystemExit(str(error)) from None
