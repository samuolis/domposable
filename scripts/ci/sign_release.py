#!/usr/bin/env python3
"""Build signed demo downloads using only dedicated secrets supplied by CI."""
from __future__ import annotations
import base64
import datetime
import hashlib
import os
from pathlib import Path
import plistlib
import shutil
import subprocess
import sys
import tempfile
import zipfile
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from version import read_version

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / ".release/artifacts"
REQUIRED = ['ANDROID_KEYSTORE_BASE64', 'ANDROID_KEYSTORE_PASSWORD', 'ANDROID_KEY_ALIAS', 'ANDROID_KEY_PASSWORD', 'ANDROID_CERT_SHA256', 'IOS_CERTIFICATE_BASE64', 'IOS_CERTIFICATE_PASSWORD', 'IOS_PROFILE_BASE64', 'IOS_TEAM_ID']

def run(*args, capture=False, env=None):
    result = subprocess.run(args, cwd=ROOT, env=env, stdout=subprocess.PIPE if capture else None, stderr=subprocess.PIPE if capture else None)
    if result.returncode: raise RuntimeError(f"{args[0]} failed; exit {result.returncode}")
    return result.stdout if capture else b""

def verify_profile(profile):
    entitlements = profile['Entitlements']
    if profile.get('ProvisionsAllDevices') or not profile.get('ProvisionedDevices'): raise RuntimeError("Use a dedicated Ad Hoc device profile")
    if entitlements.get('get-task-allow', False): raise RuntimeError("Development profiles cannot sign release downloads")
    if entitlements['application-identifier'] != os.environ['IOS_TEAM_ID'] + '.io.github.samuolis.domposable.demo': raise RuntimeError("Profile bundle identifier or team is incorrect")
    if profile['ExpirationDate'] <= datetime.datetime.now(datetime.timezone.utc).replace(tzinfo=None): raise RuntimeError("Provisioning profile expired")

def build():
    missing = [key for key in REQUIRED if not os.environ.get(key)]
    if missing: raise RuntimeError("Configure dedicated release secrets: " + ", ".join(missing))
    if sys.platform != 'darwin': raise RuntimeError("Signed releases require the macOS runner")
    ASSETS.mkdir(parents=True, exist_ok=True)
    version, build_number = read_version(ROOT / 'version.properties')
    with tempfile.TemporaryDirectory(prefix='domposable-signing-') as temporary:
        directory = Path(temporary)
        for name, key in [('android.keystore','ANDROID_KEYSTORE_BASE64'), ('ios.p12','IOS_CERTIFICATE_BASE64'), ('ios.mobileprovision','IOS_PROFILE_BASE64')]:
            path = directory / name; path.write_bytes(base64.b64decode(os.environ[key], validate=True)); path.chmod(0o600)
        env = os.environ.copy(); env['ANDROID_KEYSTORE_PATH'] = str(directory / 'android.keystore')
        run('./gradlew', ':androidApp:assembleRelease', ':demo:linkReleaseFrameworkIosArm64', ':demo:linkReleaseFrameworkIosSimulatorArm64', '--console=plain', env=env)
        apk = ROOT / 'androidApp/build/outputs/apk/release/androidApp-release.apk'
        sdk = Path(os.environ.get('ANDROID_HOME', os.environ.get('ANDROID_SDK_ROOT', '')))
        signer = sorted((sdk / 'build-tools').glob('*/apksigner'), key=lambda p: tuple(int(n) for n in p.parent.name.split('.') if n.isdigit()))[-1]
        verification = run(str(signer), 'verify', '--verbose', '--print-certs', str(apk), capture=True).decode()
        expected = os.environ['ANDROID_CERT_SHA256'].replace(':','').lower()
        if not any(line.endswith(expected) for line in verification.lower().splitlines() if 'certificate sha-256 digest:' in line): raise RuntimeError("APK certificate fingerprint differs")
        analyzer = sdk / 'cmdline-tools/latest/bin/apkanalyzer'
        if not analyzer.is_file(): analyzer = next((sdk / 'cmdline-tools').glob('*/bin/apkanalyzer'))
        if run(str(analyzer),'manifest','version-name',str(apk),capture=True).decode().strip() != str(version): raise RuntimeError("APK version name differs")
        if run(str(analyzer),'manifest','version-code',str(apk),capture=True).decode().strip() != str(build_number): raise RuntimeError("APK build number differs")
        shutil.copy2(apk, ASSETS / f'domposable-{version}.apk')
        profile = plistlib.loads(run('security', 'cms', '-D', '-i', str(directory / 'ios.mobileprovision'), capture=True))
        verify_profile(profile)
        keychain = directory / 'release.keychain-db'
        password = os.urandom(32).hex()
        original = run('security','list-keychains','-d','user',capture=True).decode()
        import shlex
        keychains = shlex.split(original)
        profiles = Path.home() / 'Library/MobileDevice/Provisioning Profiles'; profiles.mkdir(parents=True, exist_ok=True)
        installed = profiles / f"domposable-{profile['UUID']}.mobileprovision"
        if installed.exists(): raise RuntimeError("Dedicated profile path already exists; refusing to replace it")
        try:
            run('security','create-keychain','-p',password,str(keychain),capture=True)
            run('security','set-keychain-settings','-lut','21600',str(keychain),capture=True)
            run('security','unlock-keychain','-p',password,str(keychain),capture=True)
            run('security','import',str(directory / 'ios.p12'),'-k',str(keychain),'-P',os.environ['IOS_CERTIFICATE_PASSWORD'],'-T','/usr/bin/codesign','-T','/usr/bin/security',capture=True)
            run('security','set-key-partition-list','-S','apple-tool:,apple:,codesign:','-s','-k',password,str(keychain),capture=True)
            run('security','list-keychains','-d','user','-s',str(keychain),*keychains,capture=True)
            identities = run('security','find-identity','-v','-p','codesigning',str(keychain),capture=True).decode()
            import re
            matches = re.findall(r'\b([A-F0-9]{40}) "Apple Distribution:', identities)
            if len(matches) != 1: raise RuntimeError("Provide exactly one Apple Distribution certificate")
            cert_hashes = {hashlib.sha1(cert).hexdigest().upper() for cert in profile['DeveloperCertificates']}
            if matches[0] not in cert_hashes: raise RuntimeError("Distribution certificate is absent from the profile")
            shutil.copy2(directory / 'ios.mobileprovision', installed)
            run(sys.executable, 'scripts/ci/generate_xcode_project.py')
            marketing = f'{version.major}.{version.minor}.{version.patch}'
            common = ['-project','iosApp/DomposableDemo.xcodeproj','-scheme','DomposableDemo','-configuration','Release',f'DOMPOSABLE_MARKETING_VERSION={marketing}',f'DOMPOSABLE_BUILD_NUMBER={build_number}']
            archive = ROOT / 'build/release/DomposableDemo.xcarchive'
            run('xcodebuild','archive',*common,'-destination','generic/platform=iOS','-archivePath',str(archive),'CODE_SIGN_STYLE=Manual',f'DEVELOPMENT_TEAM={os.environ["IOS_TEAM_ID"]}',f'CODE_SIGN_IDENTITY={matches[0]}',f'PROVISIONING_PROFILE_SPECIFIER={profile["UUID"]}')
            options = directory / 'ExportOptions.plist'
            options.write_bytes(plistlib.dumps({'method':'release-testing','teamID':os.environ['IOS_TEAM_ID'],'signingStyle':'manual','signingCertificate':matches[0],'provisioningProfiles':{'io.github.samuolis.domposable.demo':profile['UUID']},'stripSwiftSymbols':True}))
            export = ROOT / 'build/release/export'
            run('xcodebuild','-exportArchive','-archivePath',str(archive),'-exportPath',str(export),'-exportOptionsPlist',str(options))
            app = archive / 'Products/Applications/DomposableDemo.app'
            run('codesign','--verify','--deep','--strict',str(app),capture=True)
            info = plistlib.loads((app / 'Info.plist').read_bytes())
            if info['CFBundleShortVersionString'] != marketing or str(info['CFBundleVersion']) != str(build_number): raise RuntimeError("iOS version differs from version.properties")
            ipa = next(export.glob('*.ipa'))
            payload = directory / 'exported-ipa'
            run('ditto','-x','-k',str(ipa),str(payload))
            exported_app = next((payload / 'Payload').glob('*.app'))
            run('codesign','--verify','--deep','--strict',str(exported_app),capture=True)
            exported_info = plistlib.loads((exported_app / 'Info.plist').read_bytes())
            if exported_info['CFBundleShortVersionString'] != marketing or str(exported_info['CFBundleVersion']) != str(build_number): raise RuntimeError("Exported IPA version differs")
            shutil.copy2(ipa, ASSETS / f'domposable-{version}.ipa')
            run('ditto','-c','-k','--keepParent',str(archive / 'dSYMs'),str(ASSETS / 'ios-dsyms.zip'))
            simulator = ROOT / 'build/release/simulator'
            run('xcodebuild','build',*common,'-destination','generic/platform=iOS Simulator','-derivedDataPath',str(simulator),'CODE_SIGNING_ALLOWED=NO')
            run('ditto','-c','-k','--keepParent',str(simulator / 'Build/Products/Release-iphonesimulator/DomposableDemo.app'),str(ASSETS / 'ios-simulator.app.zip'))
        finally:
            run('security','list-keychains','-d','user','-s',*keychains,capture=True)
            subprocess.run(['security','delete-keychain',str(keychain)],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
            installed.unlink(missing_ok=True)

if __name__ == '__main__':
    try: build()
    except (RuntimeError, KeyError, ValueError) as error: raise SystemExit(str(error)) from None
