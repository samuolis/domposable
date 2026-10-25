#!/usr/bin/env python3
"""Exercise the published Gradle task from an independent Kotlin DSL consumer."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[2]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--repository', help='Override the consumer Maven repository, for post-release verification')
args = parser.parse_args()
command = [str(root / 'gradlew'), '-p', str(root / 'export-consumer-fixture'),
           'exportPublicPages', '--configuration-cache', '--console=plain']
if args.repository:
    command.append(f'-Pdomposable.consumer.repository={args.repository}')
subprocess.run(command, cwd=root, check=True)
second = subprocess.run(command, cwd=root, check=True, text=True, stdout=subprocess.PIPE)
print(second.stdout)
assert 'Reusing configuration cache.' in second.stdout
output = root / 'export-consumer-fixture/build/exported'
manifest = json.loads((output / 'domposable-export.json').read_text())
assert [page['path'] for page in manifest['pages']] == ['/about/', '/events/42']
for entry, title in zip(manifest['pages'], ['About', 'Event 42']):
    body = (output / entry['path'].strip('/') / 'index.html').read_bytes()
    assert hashlib.sha256(body).hexdigest() == entry['sha256']
    html = body.decode()
    assert f'<h1>{title}</h1>' in html
    assert 'rgb(1, 2, 3)' in html
    assert '<button inert="">' in html
    assert f'href="https://example.com{entry["path"]}"' in html
print('Published Kotlin Gradle consumer: both routes and configuration-cache reuse verified')
inventory = (output / 'domposable-export.json').read_bytes()
invalid = output.parent / 'invalid-public-pages.txt'
for contents, message in [('# Only comments\n\n', 'Declare public pages'),
                          ('/events/42?share=secret\n', 'Invalid export path')]:
    invalid.write_text(contents)
    failed = subprocess.run(command + [f'-Pexport.pagesFile={invalid}'], cwd=root,
                            text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    assert failed.returncode != 0, failed.stdout
    assert message in failed.stdout, failed.stdout
    assert (output / 'domposable-export.json').read_bytes() == inventory
print('TXT configuration: comments, blank lines, empty input and unsafe routes verified')
