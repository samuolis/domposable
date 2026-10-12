#!/usr/bin/env python3
"""Inventory reviewed Kotlin ABI dumps and report changes against Git revisions.

The Kotlin Gradle plugin validates compiled binaries. This script makes baseline
updates visible; it deliberately does not classify text diffs as binary compatible.
"""

from __future__ import annotations

import argparse
import difflib
import json
from pathlib import Path
import re
import subprocess


API_PATH = re.compile(r"domposable-[^/]+/api/(?:[^/]+/)?[^/]+\.api")
RELEASE_TAG = re.compile(r"v(\d+)\.(\d+)\.(\d+)")


def git(root: Path, *args: str) -> str:
    return subprocess.check_output(["git", "-C", str(root), *args], text=True)


def revision(root: Path, ref: str) -> str:
    return git(root, "rev-parse", "--verify", "--end-of-options", f"{ref}^{{commit}}").strip()


def latest_release(root: Path, head: str) -> str:
    tags = git(root, "tag", "--merged", head, "--list", "v*").splitlines()
    releases = [(tuple(map(int, match.groups())), tag) for tag in tags if (match := RELEASE_TAG.fullmatch(tag))]
    if not releases:
        raise ValueError("No ancestor semver release tag found. Fetch full history and tags before reporting ABI changes.")
    return max(releases)[1]


def expected_paths(policy: dict) -> list[str]:
    paths = []
    for module in policy["runtime_modules"]:
        paths.extend([
            f"{module}/api/android/{module}.api",
            f"{module}/api/jvm/{module}.api",
            f"{module}/api/{module}.klib.api",
        ])
    paths.extend(f"{module}/api/{module}.api" for module in policy["processor_modules"])
    if len(paths) != len(set(paths)) or not paths:
        raise ValueError("Binary API policy must contain distinct published modules.")
    return sorted(paths)


def inventory(root: Path, policy: dict) -> dict[str, str]:
    expected = set(expected_paths(policy))
    actual = {p.relative_to(root).as_posix() for p in root.glob("domposable-*/api/**/*.api")}
    if actual != expected:
        raise ValueError(f"ABI inventory mismatch: missing={sorted(expected - actual)}, unexpected={sorted(actual - expected)}")
    declared = set(policy["runtime_modules"] + policy["processor_modules"])
    published = {
        p.parent.name for p in root.glob("domposable-*/build.gradle.kts")
        if "alias(libs.plugins.maven.publish)" in p.read_text()
    }
    if published != declared:
        raise ValueError(f"Published modules differ from binary API policy: published={sorted(published)}, policy={sorted(declared)}")
    dumps = {}
    for path in sorted(expected):
        content = (root / path).read_text(encoding="utf-8")
        if path.endswith(".klib.api"):
            match = re.search(r"^// Targets: \[([^]]+)\]$", content, re.MULTILINE)
            targets = {part.strip() for part in match.group(1).split(",")} if match else set()
            if targets != set(policy["klib_targets"]) or not re.search(r"^\S.*(?:class|interface|fun|object) ", content, re.MULTILINE):
                raise ValueError(f"Incomplete Klib ABI dump: {path}")
        elif not re.search(r"^public .* (?:class|interface) ", content, re.MULTILINE):
            raise ValueError(f"Empty or malformed JVM/Android ABI dump: {path}")
        dumps[path] = content
    return dumps


def dumps_at(root: Path, sha: str) -> dict[str, str]:
    paths = git(root, "ls-tree", "-r", "--name-only", sha).splitlines()
    return {p: git(root, "show", f"{sha}:{p}") for p in paths if API_PATH.fullmatch(p)}


def compare(old: dict[str, str], current: dict[str, str]) -> tuple[list[dict], str]:
    changes, diffs = [], []
    for path in sorted(old.keys() | current.keys()):
        before, after = old.get(path, ""), current.get(path, "")
        if before == after:
            continue
        diff = list(difflib.unified_diff(before.splitlines(True), after.splitlines(True), fromfile=f"base/{path}", tofile=f"head/{path}"))
        changes.append({
            "path": path,
            "added_lines": sum(line.startswith("+") and not line.startswith("+++") for line in diff),
            "removed_lines": sum(line.startswith("-") and not line.startswith("---") for line in diff),
        })
        diffs.extend(diff)
    return changes, "".join(diffs)


def report(root: Path, base: str, head: str, release: str | None, output: Path) -> str:
    policy = json.loads((root / "config/binary-api.json").read_text())
    current = inventory(root, policy)
    head_sha, base_sha = revision(root, head), revision(root, base)
    release_ref = release or latest_release(root, head_sha)
    release_sha = revision(root, release_ref)
    results = {"head": head_sha, "baseline_count": len(current), "comparisons": {}}
    lines = ["## Binary API review", "", f"Validated inventory: **{len(current)} ABI dumps** across **{len(policy['runtime_modules']) + len(policy['processor_modules'])} published modules**.", "", "Kotlin's `binaryApiCheck` validates compiled signatures against these reviewed baselines. The diffs below describe baseline changes, including previously reviewed unreleased changes; they do not establish semantic or source compatibility.", ""]
    output.mkdir(parents=True, exist_ok=True)
    for label, ref, sha in [("pr-base", base, base_sha), ("release", release_ref, release_sha)]:
        changes, diff = compare(dumps_at(root, sha), current)
        results["comparisons"][label] = {"ref": ref, "sha": sha, "changes": changes}
        (output / f"{label}.diff").write_text(diff, encoding="utf-8")
        lines.extend([f"### Compared with {label}: `{ref}` (`{sha[:12]}`)", ""])
        if changes:
            lines.extend(["| Baseline | Added lines | Removed lines |", "| --- | ---: | ---: |"])
            lines.extend(f"| `{change['path']}` | {change['added_lines']} | {change['removed_lines']} |" for change in changes)
        else:
            lines.append("No ABI baseline changes.")
        lines.append("")
    lines.append("Any baseline update in a PR requires an **API compatibility** explanation of classification, migration and release impact. Full diffs and compiled ABI evidence are retained in the `binary-api` artifact.")
    markdown = "\n".join(lines) + "\n"
    (output / "report.json").write_text(json.dumps(results, indent=2) + "\n", encoding="utf-8")
    (output / "report.md").write_text(markdown, encoding="utf-8")
    return markdown


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base", required=True)
    parser.add_argument("--head", default="HEAD")
    parser.add_argument("--release")
    parser.add_argument("--output", type=Path, default=Path("build/reports/binary-api"))
    parser.add_argument("--summary", type=Path)
    args = parser.parse_args()
    try:
        markdown = report(Path.cwd(), args.base, args.head, args.release, args.output)
    except (ValueError, subprocess.CalledProcessError) as error:
        raise SystemExit(f"Binary API report failed: {error}") from error
    if args.summary:
        with args.summary.open("a", encoding="utf-8") as summary:
            summary.write(markdown)
    print(markdown)


if __name__ == "__main__":
    main()
