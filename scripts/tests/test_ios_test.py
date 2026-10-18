import importlib.util
import json
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import Mock, patch

spec = importlib.util.spec_from_file_location('ios_test', Path(__file__).resolve().parents[1] / 'ci/ios_test.py')
ios = importlib.util.module_from_spec(spec)
spec.loader.exec_module(ios)


class SimulatorBuildOverlapTest(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        self.root = patch.object(ios, 'root', Path(temporary.name))
        self.root.start()
        self.addCleanup(self.root.stop)
        self.events = []
        self.output = patch.object(ios, 'output', side_effect=[
            json.dumps({'devicetypes': [{'identifier': 'com.apple.CoreSimulator.SimDeviceType.iPad-mini-A17-Pro'}]}),
            json.dumps({'runtimes': [{'isAvailable': True, 'version': '26.5', 'name': 'iOS 26.5', 'identifier': 'runtime'}]}),
            'isolated-simulator',
        ])
        self.output.start()
        self.addCleanup(self.output.stop)
        self.run = patch.object(ios.subprocess, 'run', side_effect=lambda command, **kwargs: self.events.append(command))
        self.run.start()
        self.addCleanup(self.run.stop)

    def test_boot_overlaps_build_but_tests_wait_for_framework(self):
        framework = Mock(args=['gradle'])
        framework.wait.side_effect = lambda **kwargs: self.events.append(['framework-ready']) or 0
        ios.run_tests(framework)
        boot = self.events.index(['xcrun', 'simctl', 'bootstatus', 'isolated-simulator', '-b'])
        ready = self.events.index(['framework-ready'])
        test = next(i for i, event in enumerate(self.events) if event[0] == 'xcodebuild')
        self.assertLess(boot, ready)
        self.assertLess(ready, test)
        self.assertEqual(['xcrun', 'simctl', 'delete', 'isolated-simulator'], self.events[-1])

    def test_failed_framework_deletes_simulator_without_running_tests(self):
        framework = Mock(args=['gradle'])
        framework.wait.return_value = 1
        with self.assertRaises(subprocess.CalledProcessError):
            ios.run_tests(framework)
        self.assertFalse(any(event[0] == 'xcodebuild' for event in self.events))
        self.assertEqual(['xcrun', 'simctl', 'delete', 'isolated-simulator'], self.events[-1])

    def test_boot_failure_stops_and_reaps_unfinished_framework(self):
        framework = Mock()
        framework.poll.return_value = None
        error = subprocess.CalledProcessError(1, ['boot'])
        def run(command, **kwargs):
            self.events.append(command)
            if command[:3] == ['xcrun', 'simctl', 'bootstatus']:
                raise error
        ios.subprocess.run.side_effect = run
        with patch.object(ios.subprocess, 'Popen', return_value=framework):
            with self.assertRaises(subprocess.CalledProcessError):
                ios.main(['--build-framework'])
        self.assertEqual(['xcrun', 'simctl', 'delete', 'isolated-simulator'], self.events[-1])
        framework.terminate.assert_called_once()
        framework.wait.assert_called_once_with(timeout=30)

    def test_unresponsive_framework_is_killed_and_reaped(self):
        framework = Mock()
        framework.poll.return_value = None
        framework.wait.side_effect = [subprocess.TimeoutExpired('gradle', 30), 0]
        with patch.object(ios.subprocess, 'Popen', return_value=framework), patch.object(ios, 'run_tests', side_effect=RuntimeError('boot failed')):
            with self.assertRaises(RuntimeError):
                ios.main(['--build-framework'])
        framework.kill.assert_called_once()
        self.assertEqual(2, framework.wait.call_count)
