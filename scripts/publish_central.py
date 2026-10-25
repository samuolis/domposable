#!/usr/bin/env python3
"""Validate, then publish the preserved bundle. Persist every deployment transition."""
from __future__ import annotations
import argparse
import base64
import hashlib
import json
import os
import time
import urllib.error
import urllib.request
import uuid
from release import ASSETS, GitHub, checkpoint, read_state, verify_assets

BASE = "https://central.sonatype.com/api/v1/publisher"
STATUS_TIMEOUT_SECONDS = 30
STATUS_RETRY_DELAYS = (2, 4)

class CentralHttpError(RuntimeError):
    def __init__(self, code):
        self.code = code
        super().__init__(f"Central request failed ({code}); credentials and response bodies are not logged")


def request(path, data=b"", content_type="application/json", timeout=300):
    token = base64.b64encode(f"{os.environ['CENTRAL_USERNAME']}:{os.environ['CENTRAL_PASSWORD']}".encode()).decode()
    req = urllib.request.Request(BASE + path, data=data, headers={"Authorization": "Bearer " + token, "Content-Type": content_type}, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=timeout) as response: return response.read().decode()
    except urllib.error.HTTPError as error:
        raise CentralHttpError(error.code) from None

def request_status(deployment_id):
    path = "/status?id=" + deployment_id
    for attempt in range(len(STATUS_RETRY_DELAYS) + 1):
        try:
            return request(path, timeout=STATUS_TIMEOUT_SECONDS)
        except CentralHttpError as error:
            if error.code != 429 and not 500 <= error.code <= 599:
                raise RuntimeError(str(error)) from None
        except (urllib.error.URLError, TimeoutError):
            pass
        if attempt == len(STATUS_RETRY_DELAYS):
            raise RuntimeError("Central status polling failed after retries; rerun to resume this deployment") from None
        time.sleep(STATUS_RETRY_DELAYS[attempt])

def progress(started, message):
    print(f"{message} (elapsed {int(time.monotonic() - started)}s)", flush=True)

def publish(mode, recovered_id=None):
    started = time.monotonic()
    verify_assets(); state = read_state(); github = GitHub()
    bundle = ASSETS / "maven-bundle.zip"
    if hashlib.sha256(bundle.read_bytes()).hexdigest() != state['bundle_sha256']: raise RuntimeError("Bundle changed after staging")
    if recovered_id:
        uuid.UUID(recovered_id)
        if state.get('deployment_id') not in (None, recovered_id): raise RuntimeError("Recovery ID disagrees with saved deployment")
        state['deployment_id'] = recovered_id; checkpoint(state, github)
    if not state.get("deployment_id"):
        if state.get("central_upload_intent"):
            raise RuntimeError("Previous Central upload outcome is unknown. Locate the named deployment in Central and rerun with its deployment ID; never upload a second bundle blindly")
        state['central_upload_intent'] = f"domposable-{state['version']}-{state['bundle_sha256'][:12]}"
        checkpoint(state, github)
        boundary = uuid.uuid4().hex
        data = (f'--{boundary}\r\nContent-Disposition: form-data; name="bundle"; filename="maven-bundle.zip"\r\nContent-Type: application/octet-stream\r\n\r\n'.encode() + bundle.read_bytes() + f'\r\n--{boundary}--\r\n'.encode())
        import urllib.parse
        query = urllib.parse.urlencode({"name": state['central_upload_intent'], "publishingType": "USER_MANAGED"})
        progress(started, "Uploading immutable Maven bundle to Central")
        deployment = request("/upload?" + query, data, "multipart/form-data; boundary=" + boundary).strip().strip('"')
        uuid.UUID(deployment)
        state['deployment_id'] = deployment; checkpoint(state, github)
        progress(started, "Central accepted the bundle; waiting for validation")
    deadline = time.monotonic() + 3600
    while time.monotonic() < deadline:
        progress(started, "Checking Central deployment status")
        status = json.loads(request_status(state['deployment_id']))
        current = status['deploymentState']
        known_states = {"PENDING", "VALIDATING", "VALIDATED", "PUBLISHING", "PUBLISHED", "FAILED"}
        display_state = current if isinstance(current, str) and current in known_states else "unknown"
        progress(started, f"Central deployment state: {display_state}")
        if current != state.get('central_state'):
            state['central_state'] = current; checkpoint(state, github)
        if current == "FAILED": raise RuntimeError("Central validation failed. Inspect its deployment errors in the portal; the same version remains reserved")
        if current == "PUBLISHED": return
        if current == "VALIDATED":
            if mode == "validate": return
            if state.get('phase') not in ['main-advanced', 'github-published', 'complete']:
                raise RuntimeError("Publish only after the tested version commit has advanced main")
            progress(started, "Activating validated Central deployment")
            request("/deployment/" + state['deployment_id'])
        time.sleep(20)
    raise RuntimeError("Central is still processing. Rerun Bump and release to resume this deployment")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("mode", choices=["validate", "publish"])
    parser.add_argument("--deployment-id", default=None)
    args = parser.parse_args()
    try: publish(args.mode, args.deployment_id or None)
    except (RuntimeError, ValueError, KeyError) as error: raise SystemExit(str(error)) from None
