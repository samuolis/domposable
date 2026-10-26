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
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
import zipfile

from version import Version, read_checkpoint_version, read_version, write_version

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts/ci"))
from web_assets import distribution_hash, inventory, verify_bundle

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
    return [f"domposable-{state['version']}.apk", "website.zip", "maven-bundle.zip"]

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
    version, build = read_checkpoint_version(ROOT / "version.properties")
    if str(version) != state["version"] or build != state["build"]: raise RuntimeError("Version file disagrees with release state")

def verify_assets():
    manifest = json.loads((ASSETS / "manifest.json").read_text())
    state = read_state()
    if manifest["commit"] != state["commit"] or manifest["version"] != state["version"]: raise RuntimeError("Asset manifest belongs to another release")
    if set(manifest["sha256"]) != set(asset_names(state)): raise RuntimeError("Release asset inventory is incomplete or contains unexpected files")
    for name, checksum in manifest["sha256"].items():
        if Path(name).name != name: raise RuntimeError("Invalid manifest path")
        if hashlib.sha256((ASSETS / name).read_bytes()).hexdigest() != checksum: raise RuntimeError(f"Corrupt release asset: {name}")

def is_abandoned_draft(release):
    match = MARKER.search(release.get("body") or "")
    return bool(release.get("draft") and match and json.loads(match.group(1)).get("phase") == "abandoned")

def prepare(operation):
    github = GitHub()
    if os.environ.get("GITHUB_REF") != "refs/heads/main": raise RuntimeError("Dispatch Bump and release from main")
    run("git", "fetch", "origin", "main", "--tags")
    releases = github.request("releases?per_page=100")
    pending = []
    for release in releases:
        match = MARKER.search(release.get("body") or "")
        if match:
            state = json.loads(match.group(1))
            if state.get("phase") != "complete" and not is_abandoned_draft(release): pending.append((release, state))
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
    version = current.bump(operation)
    if version.order <= current.order: raise RuntimeError("Release versions must increase")
    candidate_build = version.android_code
    if candidate_build <= build: raise RuntimeError("Android version codes must increase")
    if any(release["tag_name"] == f"v{version}" and not is_abandoned_draft(release) for release in releases): raise RuntimeError("Version already reserved")
    base = run("git", "rev-parse", "HEAD", capture=True)
    branch = f"release-candidate/v{version}-{os.environ['GITHUB_RUN_ID']}"
    existing = run("git", "ls-remote", "origin", f"refs/heads/{branch}", capture=True)
    if existing:
        commit = existing.split()[0]
        run("git", "fetch", "origin", branch)
        candidate_base = run("git", "rev-parse", commit + "^", capture=True)
        if candidate_base != base:
            raise RuntimeError("Unstaged candidate belongs to older main; dispatch a fresh release from current main")
        run("git", "checkout", "--detach", commit)
        candidate_version, candidate_build = read_version(ROOT / "version.properties", require_version_only=True)
        if candidate_version != version:
            raise RuntimeError("Unstaged candidate does not match the requested version")
        state = {"repository": REPOSITORY, "base": base, "commit": commit, "version": str(candidate_version), "build": candidate_build, "branch": branch, "phase": "candidate", "run_id": os.environ["GITHUB_RUN_ID"]}
        write_state(state); validate_candidate(state)
        output(commit=commit, version=str(candidate_version), resume=False, branch=branch)
        return
    run("git", "config", "user.name", "Domposable Release")
    run("git", "config", "user.email", "domposable-release[bot]@users.noreply.github.com")
    write_version(ROOT / "version.properties", version)
    run("git", "add", "version.properties")
    run("git", "-c", "commit.gpgSign=false", "commit", "-m", f"chore(release): {version}")
    commit = run("git", "rev-parse", "HEAD", capture=True)
    state = {"repository": REPOSITORY, "base": base, "commit": commit, "version": str(version), "build": candidate_build, "branch": branch, "phase": "candidate", "run_id": os.environ.get("GITHUB_RUN_ID")}
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
                state["ci_run_id"] = latest["id"]; checkpoint(state)
                output(ci_run_id=latest["id"])
                return
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

def package_site(site_dir):
    state = read_state()
    site_dir = Path(site_dir)
    if not site_dir.is_absolute(): site_dir = ROOT / site_dir
    if site_dir.is_symlink() or not site_dir.is_dir():
        raise RuntimeError("Candidate website artifact is missing or unsafe")
    site_dir = site_dir.resolve(strict=True)
    files = []
    for path in site_dir.rglob("*"):
        if path.is_symlink(): raise RuntimeError("Candidate website contains a symbolic link")
        if path.is_file():
            relative = path.relative_to(site_dir)
            if relative.is_absolute() or ".." in relative.parts:
                raise RuntimeError("Candidate website contains an unsafe path")
            if not path.resolve(strict=True).is_relative_to(site_dir):
                raise RuntimeError("Candidate website path escapes its artifact directory")
            files.append((path, relative))
        elif not path.is_dir():
            raise RuntimeError("Candidate website contains a non-file entry")
    metadata_path = site_dir / "release.json"
    try:
        metadata = json.loads(metadata_path.read_text())
    except (OSError, json.JSONDecodeError):
        raise RuntimeError("Candidate website artifact has no valid release metadata") from None
    expected = {
        "version": state["version"],
        "build": state["build"],
        "commit": state["commit"],
        "status": "release",
    }
    if not isinstance(metadata, dict) or any(metadata.get(key) != value for key, value in expected.items()):
        raise RuntimeError("Candidate website metadata does not match the release state")
    web = metadata.get("web")
    if type(metadata.get("build")) is not int or not isinstance(web, dict) or not {"html", "wasm"} <= web.keys():
        raise RuntimeError("Candidate website metadata is incomplete")
    for required in (site_dir / "index.html", site_dir / "api/index.html"):
        if not required.is_file(): raise RuntimeError(f"Candidate website is missing {required.relative_to(site_dir)}")
    api = metadata.get("api")
    try:
        api_files = inventory(site_dir / "api", exclude_manifest=False)
    except OSError:
        raise RuntimeError("Candidate website has an invalid API tree") from None
    if not isinstance(api, dict) or api.get("files") != api_files or api.get("sha256") != distribution_hash(api_files):
        raise RuntimeError("Candidate website API tree does not match its release metadata")
    for name, directory in (("html", "demo"), ("wasm", "wasm")):
        try:
            verify_bundle(site_dir / directory, web[name])
        except (OSError, ValueError, TypeError, KeyError):
            raise RuntimeError(f"Candidate website has an invalid {name} browser bundle") from None
    if not files: raise RuntimeError("Candidate website artifact is empty")
    target = ASSETS / "website.zip"
    target.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(target, "w", zipfile.ZIP_DEFLATED) as archive:
        for path, relative in sorted(files, key=lambda item: item[1].as_posix()):
            archive.write(path, relative.as_posix())

def stage():
    state = read_state(); validate_candidate(state); verify_assets()
    github = GitHub()
    release = github.request("releases", "POST", {"tag_name": f"v{state['version']}", "target_commitish": state["commit"], "name": f"Domposable {state['version']}", "body": release_body(state), "draft": True, "prerelease": False})
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
    github.request(f"releases/{state['release_id']}", "PATCH", {"tag_name": f"v{state['version']}", "target_commitish": state["commit"], "draft": False, "prerelease": False, "body": release_body(state), "make_latest": "true"})
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

def abandon():
    """Retain a failed validation checkpoint without resuming its invalid bundle."""
    state = read_state(); verify_assets()
    if state.get("phase") != "staged" or state.get("central_state") != "FAILED":
        raise RuntimeError("Only a staged candidate with failed Central validation can be abandoned")
    from publish_central import request as central_request
    status = json.loads(central_request("/status?id=" + state["deployment_id"]))
    if status.get("deploymentState") != "FAILED":
        raise RuntimeError("Central must still report FAILED before abandoning a candidate")
    remote = run("git", "ls-remote", "origin", "refs/heads/main", capture=True).split()[0]
    if remote != state["base"]:
        raise RuntimeError("Main moved; reconcile the candidate before abandoning it")
    if run("git", "ls-remote", "origin", f"refs/tags/v{state['version']}", capture=True):
        raise RuntimeError("The version tag exists; reconcile it before abandoning the candidate")
    github = GitHub()
    draft = github.request(f"releases/{state['release_id']}")
    if not draft.get("draft"):
        raise RuntimeError("Published releases cannot be abandoned")
    state["phase"] = "abandoned"; checkpoint(state, github)
    github.request(f"releases/{state['release_id']}", "PATCH", {"name": f"Domposable {state['version']} — failed validation archive", "tag_name": f"failed-v{state['version']}-{state['run_id']}"})

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("command", choices=["prepare", "wait-ci", "package-site", "manifest", "stage", "advance-main", "publish-github", "extract-website", "complete", "abandon"])
    parser.add_argument("--bump", default="patch", choices=["patch", "minor", "major"])
    parser.add_argument("--site-dir")
    args = parser.parse_args()
    try:
        if args.command == "prepare": prepare(args.bump)
        elif args.command == "package-site":
            if not args.site_dir: parser.error("package-site requires --site-dir")
            package_site(args.site_dir)
        else: {"wait-ci": wait_ci, "manifest": manifest, "stage": stage, "advance-main": advance_main, "publish-github": publish_github, "extract-website": extract_website, "complete": complete, "abandon": abandon}[args.command]()
    except (RuntimeError, ValueError, KeyError) as error:
        raise SystemExit(str(error)) from None
