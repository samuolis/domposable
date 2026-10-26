"""Exercise the actual final workflow gate across routine and release validation."""
import os
from pathlib import Path
import subprocess
import unittest
import yaml


class CiGateTest(unittest.TestCase):
    def test_all_remaining_jobs_are_required_and_recording_cannot_pass(self):
        workflow = yaml.safe_load((Path(__file__).parents[2]/'.github/workflows/ci.yml').read_text())
        step = workflow['jobs']['passed']['steps'][0]
        self.assertNotIn('native', workflow['jobs'])
        consumers = str(workflow['jobs']['consumers']['steps'])
        for target in ('IosArm64', 'IosSimulatorArm64'):
            self.assertIn(f':demo:compileKotlin{target}', consumers)
            self.assertIn(f':consumer-fixture:compileKotlin{target}', consumers)
        self.assertIn('binary-api', workflow['jobs']['passed']['needs'])
        required = set(step['env']) - {'RECORD'}
        base = dict.fromkeys(required, 'success') | {'RECORD': 'false'}

        def accepted(changes):
            return subprocess.run(['bash', '-c', step['run']], env=os.environ | base | changes, capture_output=True).returncode == 0

        self.assertTrue(accepted({}))
        self.assertFalse(accepted({'RECORD': 'true'}))
        for check in required:
            for result in ('failure', 'skipped', 'cancelled'):
                with self.subTest(check=check, result=result):
                    self.assertFalse(accepted({check: result}))

    def test_release_site_artifact_preserves_hidden_files_only_on_candidate_refs(self):
        workflow = yaml.safe_load((Path(__file__).parents[2]/'.github/workflows/ci.yml').read_text())
        steps = workflow['jobs']['portable']['steps']
        upload = next(step for step in steps if step.get('with', {}).get('name') == 'release-site')
        self.assertEqual(upload['if'], "startsWith(github.ref, 'refs/heads/release-candidate/')")
        self.assertEqual(upload['with']['path'], 'build/site/')
        self.assertTrue(upload['with']['include-hidden-files'])

    def test_release_validates_downloaded_site_before_signing_or_building(self):
        workflow = yaml.safe_load((Path(__file__).parents[2]/'.github/workflows/release.yml').read_text())
        steps = workflow['jobs']['release']['steps']
        names = [step.get('name') for step in steps]
        self.assertLess(names.index('Download website from the successful candidate CI run'), names.index('Verify and package the candidate website'))
        self.assertLess(names.index('Verify and package the candidate website'), names.index('Build signed Android demo download'))
        self.assertLess(names.index('Verify and package the candidate website'), names.index('Build signed publications'))
