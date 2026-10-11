import hashlib
import importlib.util
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import zipfile

spec = importlib.util.spec_from_file_location('package_publications', Path(__file__).resolve().parents[1] / 'ci/package_publications.py')
packaging = importlib.util.module_from_spec(spec)
spec.loader.exec_module(packaging)


class PublicationAttachmentsTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        (self.root / 'version.properties').write_text('VERSION=0.1.0\nBUILD_NUMBER=1\n')
        self.repository = self.root / 'repository'
        self.folder = self.repository / 'io/github/samuolis/domposable/domposable-ui/0.1.0'
        self.folder.mkdir(parents=True)
        self.target = self.root / 'bundle.zip'
        self.payloads = {
            'domposable-ui-0.1.0.pom': b'<project xmlns="http://maven.apache.org/POM/4.0.0"><groupId>io.github.samuolis.domposable</groupId><artifactId>domposable-ui</artifactId><version>0.1.0</version><name>UI</name><description>UI</description><url>https://example.test</url><licenses/><developers/><scm/></project>',
            'domposable-ui-0.1.0.jar': b'library',
            'domposable-ui-0.1.0-sources.jar': b'sources',
            'domposable-ui-0.1.0-javadoc.jar': b'documentation',
            'domposable-ui-0.1.0-kotlin-tooling-metadata.json': b'{"schemaVersion":"1.0.0"}',
            'domposable-ui-0.1.0-kotlin_resources.kotlin_resources.zip': b'resources',
            'domposable-ui-0.1.0-future.metadata': b'future attachment',
        }
        for name, data in self.payloads.items():
            (self.folder / name).write_bytes(data)
            (self.folder / (name + '.asc')).write_bytes(b'signature')

    def tearDown(self):
        self.temp.cleanup()

    def test_every_attachment_and_signature_has_central_checksums(self):
        with patch.multiple(packaging, ROOT=self.root, MODULES={'ui'}):
            packaging.package(self.repository, self.target, signed=False)
        with zipfile.ZipFile(self.target) as archive:
            for name in archive.namelist():
                if name.endswith(('.md5', '.sha1', '.sha256', '.sha512')):
                    continue
                payload = archive.read(name)
                for algorithm in ['md5', 'sha1', 'sha256', 'sha512']:
                    self.assertEqual(archive.read(name + '.' + algorithm).decode(), hashlib.new(algorithm, payload).hexdigest())

    def test_unsigned_resource_attachment_is_rejected_in_release_mode(self):
        (self.folder / 'domposable-ui-0.1.0-kotlin_resources.kotlin_resources.zip.asc').unlink()
        with patch.multiple(packaging, ROOT=self.root, MODULES={'ui'}), patch('subprocess.run'):
            with self.assertRaisesRegex(ValueError, 'Missing signature: .*resources.*zip'):
                packaging.package(self.repository, self.target, signed=True)
