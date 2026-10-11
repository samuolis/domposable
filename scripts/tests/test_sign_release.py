import importlib.util
import os
from pathlib import Path
import sys
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location('sign_release', Path(__file__).resolve().parents[1] / 'ci/sign_release.py')
sign_release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(sign_release)

class AndroidReleaseVerificationTest(unittest.TestCase):
    def verify(self, digest='abcd', version='0.1.0', build='1', signer='Signer #1'):
        output = [f'{signer} certificate SHA-256 digest: {digest}\n'.encode(), version.encode(), build.encode()]
        with patch.dict(os.environ, {'ANDROID_CERT_SHA256': 'AB:CD'}), patch.object(sign_release, 'run', side_effect=output):
            sign_release.verify_apk('apksigner', 'apkanalyzer', 'demo.apk', '0.1.0', 1)
    def test_expected_identity_and_version_pass(self):
        self.verify()
    def test_build_tools_37_signer_prefix_passes(self):
        self.verify(signer='V2 Signer:')
    def test_build_tools_37_wrong_identity_is_rejected(self):
        with self.assertRaisesRegex(RuntimeError, 'fingerprint'):
            self.verify(digest='0000abcd', signer='V2 Signer:')
    def test_another_signing_identity_is_rejected(self):
        with self.assertRaisesRegex(RuntimeError, 'fingerprint'): self.verify(digest='0000abcd')
    def test_wrong_version_is_rejected(self):
        with self.assertRaisesRegex(RuntimeError, 'version name'): self.verify(version='0.0.0')
    def test_wrong_build_is_rejected(self):
        with self.assertRaisesRegex(RuntimeError, 'build number'): self.verify(build='2')
