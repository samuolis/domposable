#!/usr/bin/env python3
"""One-action release orchestration. Only version.properties may update main."""
from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import time
import urllib.error
import urllib.parse
import urllib.request
import zipfile

from version import Version, read_version, write_version

ROOT = Path(__file__).resolve().parents[1]
STATE = ROOT / ".release/state.json"
ASSETS = ROOT / ".release/artifacts"
MARKER = re.compile(r"<!-- domposable-release (.+?) -->")
REPOSITORY = "samuolis/domposable"

class SafeRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, request, fp, code, msg, headers, newurl):
        redirected = super().redirect_request(request, fp, code, msg, headers, newurl)
        if urllib.parse.urlparse(newurl).scheme != "https": raise RuntimeError("Download redirect must use HTTPS")
        if urllib.parse.urlparse(request.full_url).netloc != urllib.parse.urlparse(newurl).netloc:
            redirected.remove_header("Authorization")
        return redirected

OPENER = urllib.request.build_opener(SafeRedirect())

def run(*command, capture=False):
    result = subprocess.run(command, cwd=ROOT, text=True, stdout=subprocess.PIPE if capture else None, stderr=subprocess.PIPE if capture else None)
    if result.returncode:
        raise RuntimeError(f"{command[0]} failed with exit code {result.returncode}")
    return result.stdout.strip() if capture else None

class GitHub:
    def __init__(self):
        self.token = os.environ["GH_TOKEN"]
        if os.environ.get("GITHUB_REPOSITORY") != REPOSITORY:
            raise RuntimeError("Release workflow is restricted to samuolis/domposable")

    def request(self, path, method="GET", data=None, binary=None):
        url = path if path.startswith("https://") else f"https://api.github.com/repos/{REPOSITORY}/{path}"
        headers = {"Authorization": f"Bearer {self.token}", "Accept": "application/vnd.github+json", "X-GitHub-Api-Version": "2022-11-28"}
        payload = json.dumps(data).encode() if data is not None else binary
        if payload is not None: headers["Content-Type"] = "application/json" if binary is None else "application/octet-stream"
        try:
            with OPENER.open(urllib.request.Request(url, data=payload, headers=headers, method=method), timeout=60) as response:
                value = response.read()
                return json.loads(value) if value else None
        except urllib.error.HTTPError as error:
            raise RuntimeError(f"GitHub {method} request failed ({error.code})") from None

    def upload(self, release_id, path, replace=False):
        assets = self.request(f"releases/{release_id}/assets?per_page=100")
        old = next((asset for asset in assets if asset["name"] == path.name), None)
        if old is not None and old.get("state") == "starter":
            self.request(f"releases/assets/{old['id']}", "DELETE")
            old = None
        if old is not None:
            if not replace:
                digest = old.get("digest")
                expected = "sha256:" + hashlib.sha256(path.read_bytes()).hexdigest()
                if digest != expected: raise RuntimeError(f"Immutable release asset differs or has no digest: {path.name}")
                return
            self.request(f"releases/assets/{old['id']}", "DELETE")
        url = f"https://uploads.github.com/repos/{REPOSITORY}/releases/{release_id}/assets?{urllib.parse.urlencode({'name': path.name})}"
        self.request(url, "POST", binary=path.read_bytes())

    def download_assets(self, release):
        ASSETS.mkdir(parents=True, exist_ok=True)
        for asset in release["assets"]:
            name = asset["name"]
            if Path(name).name != name or not re.fullmatch(r"[A-Za-z0-9._-]+", name): raise RuntimeError("Invalid release asset name")
            request = urllib.request.Request(asset["url"], headers={"Authorization": f"Bearer {self.token}", "Accept": "application/octet-stream"})
            with OPENER.open(request, timeout=120) as response:
                (ASSETS / name).write_bytes(response.read())

def read_state():
    state = json.loads(STATE.read_text())
    validate_state(state)
    return state

def validate_state(state):
    if state.get("repository") != REPOSITORY: raise RuntimeError("Release state belongs to another repository")
    for name in ["base", "commit"]:
        if not re.fullmatch(r"[a-f0-9]{40}", state.get(name, "")): raise RuntimeError("Invalid release commit")
    if not re.fullmatch(r"release-candidate/[A-Za-z0-9._/-]+", state.get("branch", "")): raise RuntimeError("Invalid candidate branch")
    Version.parse(state["version"])
    if not isinstance(state.get("build"), int) or state["build"] < 1: raise RuntimeError("Invalid release build")

def asset_names(state):
    return [f"domposable-{state['version']}.apk", f"domposable-{state['version']}.ipa", "ios-simulator.app.zip", "ios-dsyms.zip", "website.zip", "maven-bundle.zip"]

def write_state(state):
    STATE.parent.mkdir(parents=True, exist_ok=True)
    temporary = STATE.with_suffix(".tmp")
    temporary.write_text(json.dumps(state, indent=2) + "\n")
    temporary.replace(STATE)

def release_body(state):
    return f"Domposable {state['version']}\n\nLibraries, demo downloads and documentation are built from `{state['commit']}`.\n\n<!-- domposable-release {json.dumps(state, sort_keys=True, separators=(',', ':'))} -->"

def checkpoint(state, github=None):
    write_state(state)
    if state.get("release_id"):
        github = github or GitHub()
        github.request(f"releases/{state['release_id']}", "PATCH", {"body": release_body(state)})
        public = ASSETS / "release-state.json"
        public.write_text(STATE.read_text())
        github.upload(state["release_id"], public, replace=True)

def output(**values):
    target = os.environ.get("GITHUB_OUTPUT")
    if target:
        with open(target, "a") as stream:
            for key, value in values.items():
                if "\n" in str(value): raise RuntimeError("Invalid workflow output")
                stream.write(f"{key}={str(value).lower() if isinstance(value, bool) else value}\n")
    print(json.dumps(values))

def validate_candidate(state):
    for key in ["base", "commit"]:
        if not re.fullmatch(r"[a-f0-9]{40}", state[key]): raise RuntimeError("Invalid release commit")
    if state["repository"] != REPOSITORY: raise RuntimeError("Release state belongs to another repository")
    Version.parse(state["version"])
    parent = run("git", "rev-parse", state["commit"] + "^", capture=True)
    parents = run("git", "rev-list", "--parents", "-n", "1", state["commit"], capture=True).split()
    changed = run("git", "diff", "--name-only", state["base"], state["commit"], capture=True).splitlines()
    if len(parents) != 2 or parent != state["base"] or changed != ["version.properties"]:
        raise RuntimeError("Release candidate must be one commit changing only version.properties")
    version, build = read_version(ROOT / "version.properties")
    if str(version) != state["version"] or build != state["build"]: raise RuntimeError("Version file disagrees with release state")

def verify_assets():
    manifest = json.loads((ASSETS / "manifest.json").read_text())
    state = read_state()
    if manifest["commit"] != state["commit"] or manifest["version"] != state["version"]: raise RuntimeError("Asset manifest belongs to another release")
    if set(manifest["sha256"]) != set(asset_names(state)): raise RuntimeError("Release asset inventory is incomplete or contains unexpected files")
    for name, checksum in manifest["sha256"].items():
        if Path(name).name != name: raise RuntimeError("Invalid manifest path")
        if hashlib.sha256((ASSETS / name).read_bytes()).hexdigest() != checksum: raise RuntimeError(f"Corrupt release asset: {name}")

def prepare(operation, channel):
    github = GitHub()
    if os.environ.get("GITHUB_REF") != "refs/heads/main": raise RuntimeError("Dispatch Bump and release from main")
    run("git", "fetch", "origin", "main", "--tags")
    releases = github.request("releases?per_page=100")
    pending = []
    for release in releases:
        match = MARKER.search(release.get("body") or "")
        if match:
            state = json.loads(match.group(1))
            if state.get("phase") != "complete": pending.append((release, state))
    if len(pending) > 1: raise RuntimeError("Multiple unfinished releases; reconcile their states before continuing")
    if pending:
        release, state = pending[0]
        validate_state(state)
        run("git", "fetch", "origin", state["commit"])
        run("git", "checkout", "--detach", state["commit"])
        write_state(state); validate_candidate(state)
        try:
            github.download_assets(release); verify_assets()
        except (FileNotFoundError, RuntimeError, urllib.error.URLError):
            # Recover the immutable build snapshot if draft upload was interrupted.
            run("gh", "run", "download", str(state["run_id"]), "--repo", REPOSITORY, "--name", "release-assets", "--dir", str(ASSETS))
            verify_assets()
        for path in sorted(ASSETS.iterdir()):
            if path.name != "release-state.json": github.upload(release["id"], path)
        output(commit=state["commit"], version=state["version"], resume=True, branch=state["branch"])
        return
    run("git", "checkout", "--detach", "origin/main")
    current, build = read_version(ROOT / "version.properties")
    # A version already pushed but interrupted before draft creation must be resumed.
    if build > 0 and not any(release["tag_name"] == f"v{current}" for release in releases):
        raise RuntimeError("Current version has no release checkpoint. Recover its saved candidate artifact; do not bump again")
    version = current.bump(operation, channel)
    if version.order <= current.order: raise RuntimeError("Release versions must increase")
    if any(release["tag_name"] == f"v{version}" for release in releases): raise RuntimeError("Version already reserved")
    base = run("git", "rev-parse", "HEAD", capture=True)
    branch = f"release-candidate/v{version}-{os.environ['GITHUB_RUN_ID']}"
    existing = run("git", "ls-remote", "origin", f"refs/heads/{branch}", capture=True)
    if existing:
        commit = existing.split()[0]
        run("git", "fetch", "origin", branch); run("git", "checkout", "--detach", commit)
        candidate_version, candidate_build = read_version(ROOT / "version.properties")
        state = {"repository": REPOSITORY, "base": run("git", "rev-parse", commit + "^", capture=True), "commit": commit, "version": str(candidate_version), "build": candidate_build, "branch": branch, "phase": "candidate", "run_id": os.environ["GITHUB_RUN_ID"]}
        write_state(state); validate_candidate(state)
        output(commit=commit, version=str(candidate_version), resume=False, branch=branch)
        return
    run("git", "config", "user.name", "Domposable Release")
    run("git", "config", "user.email", "domposable-release[bot]@users.noreply.github.com")
    write_version(ROOT / "version.properties", version, build + 1)
    run("git", "add", "version.properties")
    run("git", "-c", "commit.gpgSign=false", "commit", "-m", f"chore(release): {version}")
    commit = run("git", "rev-parse", "HEAD", capture=True)
    state = {"repository": REPOSITORY, "base": base, "commit": commit, "version": str(version), "build": build + 1, "branch": branch, "phase": "candidate", "run_id": os.environ.get("GITHUB_RUN_ID")}
    write_state(state); validate_candidate(state)
    run("git", "push", "origin", f"HEAD:refs/heads/{branch}")
    output(commit=commit, version=str(version), resume=False, branch=branch)

def wait_ci():
    state = read_state(); github = GitHub()
    deadline = time.monotonic() + 5400
    retried = False
    while time.monotonic() < deadline:
        runs = github.request(f"actions/workflows/ci.yml/runs?{urllib.parse.urlencode({'head_sha': state['commit'], 'branch': state['branch'], 'event': 'push', 'per_page': 100})}")["workflow_runs"]
        matches = [item for item in runs if item["head_sha"] == state["commit"] and item["head_branch"] == state["branch"]]
        if matches:
            latest = max(matches, key=lambda item: item["id"])
            if latest["status"] == "completed":
                if latest["conclusion"] != "success":
                    if not retried:
                        github.request(f"actions/runs/{latest['id']}/rerun", "POST", {})
                        retried = True; time.sleep(20); continue
                    raise RuntimeError("Candidate CI failed; main and published releases were not changed")
                state["ci_run_id"] = latest["id"]; checkpoint(state); return
        time.sleep(15)
    raise RuntimeError("Timed out waiting for candidate CI")

def manifest():
    state = read_state(); validate_candidate(state)
    required = asset_names(state)
    if any(not (ASSETS / name).is_file() for name in required): raise RuntimeError("Required signed release assets are missing")
    unexpected = {path.name for path in ASSETS.iterdir()} - set(required) - {"manifest.json", "SHA256SUMS", "release-state.json"}
    if unexpected: raise RuntimeError("Unexpected files in release assets; refusing to upload: " + ", ".join(sorted(unexpected)))
    checksums = {path.name: hashlib.sha256(path.read_bytes()).hexdigest() for path in sorted(ASSETS.iterdir()) if path.name not in ["manifest.json", "SHA256SUMS", "release-state.json"]}
    manifest = {"version": state["version"], "build": state["build"], "commit": state["commit"], "sha256": checksums}
    (ASSETS / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
    (ASSETS / "SHA256SUMS").write_text("".join(f"{checksum}  {name}\n" for name, checksum in checksums.items()))
    state["bundle_sha256"] = checksums["maven-bundle.zip"]
    state["phase"] = "staged"
    write_state(state)

def stage():
    state = read_state(); validate_candidate(state); verify_assets()
    github = GitHub()
    release = github.request("releases", "POST", {"tag_name": f"v{state['version']}", "target_commitish": state["commit"], "name": f"Domposable {state['version']}", "body": release_body(state), "draft": True, "prerelease": Version.parse(state["version"]).channel is not None})
    state["release_id"] = release["id"]; checkpoint(state, github)
    for path in sorted(ASSETS.iterdir()):
        if path.name != "release-state.json": github.upload(state["release_id"], path)

def verify_candidate_ci(state):
    identifier = state.get("ci_run_id")
    if not isinstance(identifier, int): raise RuntimeError("Candidate has no successful CI checkpoint")
    result = GitHub().request(f"actions/runs/{identifier}")
    if result.get("head_sha") != state["commit"] or result.get("conclusion") != "success" or result.get("path") != ".github/workflows/ci.yml":
        raise RuntimeError("Candidate CI does not prove success for this commit")

def advance_main():
    state = read_state(); validate_candidate(state); verify_assets(); verify_candidate_ci(state)
    remote = run("git", "ls-remote", "origin", "refs/heads/main", capture=True).split()[0]
    if remote == state["commit"]:
        state["phase"] = "main-advanced"; checkpoint(state); return
    # A resumed release already advanced main: never roll back later implementation commits.
    run("git", "fetch", "origin", "main")
    ancestor = subprocess.run(["git", "merge-base", "--is-ancestor", state["commit"], "origin/main"], cwd=ROOT).returncode == 0
    if ancestor:
        state["phase"] = "main-advanced"; checkpoint(state); return
    if remote != state["base"]: raise RuntimeError("Main advanced during release preparation; no direct push was attempted")
    # Ordinary fast-forward push is atomic and cannot overwrite a racing main update.
    run("git", "push", "origin", f"{state['commit']}:refs/heads/main")
    state["phase"] = "main-advanced"; checkpoint(state)

def publish_github():
    state = read_state(); github = GitHub(); verify_assets()
    if state.get("central_state") != "PUBLISHED": raise RuntimeError("Maven publication must succeed before exposing the GitHub release")
    state["phase"] = "github-published"
    github.request(f"releases/{state['release_id']}", "PATCH", {"draft": False, "body": release_body(state), "make_latest": "false" if Version.parse(state["version"]).channel else "true"})
    checkpoint(state, github)
    extract_website()

def extract_website():
    verify_assets()
    target = ROOT / "build/released-site"; target.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(ASSETS / "website.zip") as archive:
        for entry in archive.infolist():
            path = Path(entry.filename)
            if path.is_absolute() or ".." in path.parts: raise RuntimeError("Unsafe website archive path")
        archive.extractall(target)

def complete():
    state = read_state(); state["phase"] = "complete"; checkpoint(state)
    # Removing a temporary candidate branch never removes or changes the release tag.
    if run("git", "ls-remote", "origin", f"refs/heads/{state['branch']}", capture=True):
        run("git", "push", "origin", "--delete", state["branch"])

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("command", choices=["prepare", "wait-ci", "manifest", "stage", "advance-main", "publish-github", "extract-website", "complete"])
    parser.add_argument("--bump", default="prerelease", choices=["prerelease", "patch", "minor", "major", "stable"])
    parser.add_argument("--channel", default="alpha", choices=["alpha", "beta", "rc"])
    args = parser.parse_args()
    try:
        if args.command == "prepare": prepare(args.bump, args.channel)
        else: {"wait-ci": wait_ci, "manifest": manifest, "stage": stage, "advance-main": advance_main, "publish-github": publish_github, "extract-website": extract_website, "complete": complete}[args.command]()
    except (RuntimeError, ValueError, KeyError) as error:
        raise SystemExit(str(error)) from None
