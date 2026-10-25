import hashlib
from pathlib import Path
import socket
import sys
import tempfile
import urllib.error
import unittest
from contextlib import redirect_stdout
from io import StringIO
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
    def test_progress_logs_state_and_elapsed_without_secrets_or_response_body(self):
        self.state.update(deployment_id='d5650f77-692f-4189-96ed-28b5f40607ca',phase='main-advanced')
        output = StringIO()
        with patch.dict('os.environ', {'CENTRAL_USERNAME':'hidden-user','CENTRAL_PASSWORD':'hidden-password'}), \
             patch.object(central,'request',side_effect=['{"deploymentState":"VALIDATED"}', '', '{"deploymentState":"PUBLISHED"}']), \
             redirect_stdout(output):
            central.publish('publish')
        logs = output.getvalue()
        self.assertIn('Central deployment state: VALIDATED', logs)
        self.assertIn('Central deployment state: PUBLISHED', logs)
        self.assertIn('elapsed ', logs)
        self.assertNotIn('hidden-user', logs)
        self.assertNotIn('hidden-password', logs)
        self.assertNotIn('{"deploymentState"', logs)
    def test_status_recovers_from_dns_failure_with_bounded_timeout(self):
        deployment_id = 'd5650f77-692f-4189-96ed-28b5f40607ca'
        with patch.object(central,'request',side_effect=[urllib.error.URLError(socket.gaierror('DNS unavailable')), '{"deploymentState":"PUBLISHED"}']) as request:
            result = central.request_status(deployment_id)
        self.assertEqual(result, '{"deploymentState":"PUBLISHED"}')
        self.assertEqual(request.call_count, 2)
        self.assertTrue(all(call.kwargs['timeout'] == 30 for call in request.call_args_list))
        self.assertEqual(central.time.sleep.call_args_list, [unittest.mock.call(2)])
    def test_status_network_exhaustion_is_safe_and_resumable(self):
        error = urllib.error.URLError(socket.gaierror('private network detail'))
        with patch.object(central,'request',side_effect=error) as request:
            with self.assertRaisesRegex(RuntimeError, 'after retries') as raised:
                central.request_status('d5650f77-692f-4189-96ed-28b5f40607ca')
        self.assertEqual(request.call_count, 3)
        self.assertNotIn('private network detail', str(raised.exception))
    def test_status_retries_server_error(self):
        with patch.object(central,'request',side_effect=[central.CentralHttpError(503), '{"deploymentState":"VALIDATED"}']) as request:
            result = central.request_status('d5650f77-692f-4189-96ed-28b5f40607ca')
        self.assertEqual(result, '{"deploymentState":"VALIDATED"}')
        self.assertEqual(request.call_count, 2)
    def test_status_does_not_retry_authentication_error(self):
        with patch.object(central,'request',side_effect=central.CentralHttpError(401)) as request:
            with self.assertRaisesRegex(RuntimeError, '401') as raised:
                central.request_status('d5650f77-692f-4189-96ed-28b5f40607ca')
        request.assert_called_once()
        self.assertNotIn('response body', str(raised.exception))
    def test_malformed_status_json_is_not_retried(self):
        self.state['deployment_id'] = 'd5650f77-692f-4189-96ed-28b5f40607ca'
        with patch.object(central,'request',return_value='not json') as request:
            with self.assertRaises(ValueError): central.publish('validate')
        request.assert_called_once()
    def test_upload_and_activation_posts_are_not_retried(self):
        with patch.object(central,'request',side_effect=central.CentralHttpError(503)) as request:
            with self.assertRaises(central.CentralHttpError): central.publish('validate')
        request.assert_called_once()
        self.assertIn('/upload?', request.call_args.args[0])

        self.state.update(deployment_id='d5650f77-692f-4189-96ed-28b5f40607ca',phase='main-advanced')
        with patch.object(central,'request',side_effect=['{"deploymentState":"VALIDATED"}', central.CentralHttpError(503)]) as request:
            with self.assertRaises(central.CentralHttpError): central.publish('publish')
        self.assertEqual(request.call_count, 2)
        self.assertTrue(request.call_args_list[-1].args[0].startswith('/deployment/'))

if __name__ == '__main__': unittest.main()
