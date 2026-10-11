import hashlib
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import publish_central as central

class CentralRecoveryTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(); self.assets = Path(self.temp.name)
        (self.assets / 'maven-bundle.zip').write_bytes(b'preserved bundle')
        self.state = {'version':'0.1.0', 'bundle_sha256':hashlib.sha256(b'preserved bundle').hexdigest(), 'phase':'staged'}
        self.context = patch.multiple(central, ASSETS=self.assets)
        self.context.start()
        self.patches = [patch.object(central,'verify_assets'), patch.object(central,'read_state',return_value=self.state), patch.object(central,'checkpoint'), patch.object(central,'GitHub'), patch.object(central.time,'sleep')]
        for item in self.patches: item.start()
    def tearDown(self):
        for item in reversed(self.patches): item.stop()
        self.context.stop(); self.temp.cleanup()
    def test_unknown_upload_outcome_never_uploads_again(self):
        self.state['central_upload_intent'] = 'original-upload'
        with patch.object(central,'request') as request:
            with self.assertRaisesRegex(RuntimeError,'outcome is unknown'): central.publish('validate')
        request.assert_not_called()
    def test_existing_deployment_resumes_without_upload(self):
        self.state['deployment_id'] = 'd5650f77-692f-4189-96ed-28b5f40607ca'
        with patch.object(central,'request',return_value='{"deploymentState":"VALIDATED"}') as request:
            central.publish('validate')
        self.assertEqual(request.call_count,1)
        self.assertTrue(request.call_args.args[0].startswith('/status?id='))
    def test_publish_requires_main_advanced(self):
        self.state['deployment_id'] = 'd5650f77-692f-4189-96ed-28b5f40607ca'
        with patch.object(central,'request',return_value='{"deploymentState":"VALIDATED"}') as request:
            with self.assertRaisesRegex(RuntimeError,'advanced main'): central.publish('publish')
        self.assertFalse(any(call.args[0].startswith('/deployment/') for call in request.call_args_list))
    def test_publish_tracks_existing_id_until_complete(self):
        self.state.update(deployment_id='d5650f77-692f-4189-96ed-28b5f40607ca',phase='main-advanced')
        with patch.object(central,'request',side_effect=['{"deploymentState":"VALIDATED"}', '', '{"deploymentState":"PUBLISHED"}']) as request:
            central.publish('publish')
        self.assertEqual(self.state['central_state'],'PUBLISHED')
        self.assertFalse(any('/upload?' in call.args[0] for call in request.call_args_list))

if __name__ == '__main__': unittest.main()
