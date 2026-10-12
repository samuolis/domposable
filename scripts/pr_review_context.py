#!/usr/bin/env python3
"""Validate the required pull-request description sections."""

from __future__ import annotations

from pathlib import Path, PurePosixPath
import argparse
import re
import subprocess
from typing import Iterable


ARCHITECTURE_ROOTS = {
    "domposable-ui", "domposable-screens", "domposable-navigation3", "domposable-adaptive", "domposable-ksp", "domposable-foundation",
}
ARCHITECTURE_FILENAMES = {"settings.gradle.kts", "settings.gradle"}
BUILD_FILENAMES = {
    "build.gradle",
    "build.gradle.kts",
    "gradle.properties",
    "settings.gradle",
    "settings.gradle.kts",
}
BUILD_PATH_PREFIXES = (".github/workflows/", "build-logic/", "gradle/", "plugins/")
def git(*args: str) -> str:
    return subprocess.check_output(("git", *args), text=True)


def requires_architecture_context(paths: Iterable[str]) -> bool:
    for path in paths:
        parts = PurePosixPath(path).parts
        if path in ARCHITECTURE_FILENAMES or path.endswith("/build.gradle.kts"):
            return True
        if parts and parts[0] in ARCHITECTURE_ROOTS:
            return True
        if path.startswith("feature/base/"):
            return True
        if len(parts) >= 3 and parts[0] == "feature" and parts[2] == "screen-contract":
            return True
    return False


def requires_api_example(paths: Iterable[str]) -> bool:
    for path in paths:
        if is_api_baseline(path):
            return True
        parts = PurePosixPath(path).parts
        is_kotlin_source = path.endswith((".kt", ".kts"))
        if not is_kotlin_source or "/src/commonMain/" not in path:
            continue
        if parts and parts[0] in ARCHITECTURE_ROOTS:
            return True
        if len(parts) >= 4 and parts[0] == "feature" and parts[2] in {"api", "screen-contract"} and parts[3] == "src":
            return True
    return False


def is_api_baseline(path: str) -> bool:
    return bool(re.fullmatch(r"domposable-[^/]+/api/(?:[^/]+/)?[^/]+\.api", path))


def requires_build_example(paths: Iterable[str]) -> bool:
    for path in paths:
        if PurePosixPath(path).name in BUILD_FILENAMES or path.startswith(BUILD_PATH_PREFIXES):
            return True
    return False


def has_code_example(content: str) -> bool:
    blocks = re.findall(r"```[^\n]*\n(.*?)```", content, re.DOTALL)
    return any(
        any(line.strip() and not line.lstrip().startswith(("#", "//")) for line in block.splitlines())
        for block in blocks
    )


def parse_name_status(output: str) -> list[str]:
    changed: list[str] = []
    for line in output.splitlines():
        fields = line.split("\t")
        if len(fields) < 2:
            continue
        changed.append(fields[-1])
    return changed


def collect_changed_paths(base: str, head: str) -> list[str]:
    range_spec = f"{base}...{head}"
    return parse_name_status(git("diff", "--name-status", "--find-renames", range_spec))


def section(body: str, heading: str) -> str | None:
    pattern = re.compile(rf"^##\s+{re.escape(heading)}\s*$\n(.*?)(?=^##\s+|\Z)", re.IGNORECASE | re.MULTILINE | re.DOTALL)
    match = pattern.search(body)
    return match.group(1).strip() if match else None


def validate_description(body: str, changed_paths: Iterable[str]) -> list[str]:
    errors: list[str] = []
    summary = section(body, "Summary")
    if not summary or not re.search(r"^\s*[-*]\s+\S", summary, re.MULTILINE):
        errors.append("PR description needs a non-empty bullet under `## Summary`.")

    paths = tuple(changed_paths)
    if requires_architecture_context(paths):
        architecture = section(body, "Architecture")
        has_mobile_summary = bool(
            architecture
            and re.search(
                r"^###[ \t]+Architecture at a glance[ \t]*\r?\n(?:[ \t]*\r?\n)*[ \t]*[-*][ \t]+\S",
                architecture,
                re.IGNORECASE | re.MULTILINE,
            )
        )
        has_mermaid = bool(architecture and "```mermaid" in architecture.lower())
        if not has_mobile_summary or not has_mermaid:
            errors.append(
                "Architecture changes need a mobile-readable `### Architecture at a glance` bullet list "
                "and a Mermaid diagram under `## Architecture`."
            )
    if requires_api_example(paths):
        api_example = section(body, "API example")
        if not api_example or not has_code_example(api_example):
            errors.append("Public API changes need an API/code example under `## API example`.")
    if any(is_api_baseline(path) for path in paths):
        compatibility = section(body, "API compatibility") or ""
        required_fields = {
            "Classification": r"(?:additive|breaking|tooling-only)\b[^\n]*",
            "Migration": r"\S[^\n]*",
            "Release": r"(?:patch|minor|major)\b[^\n]*",
        }
        for field, value in required_fields.items():
            if not re.search(rf"^[ \t]*(?:[-*][ \t]+)?{field}:[ \t]*{value}$", compatibility, re.IGNORECASE | re.MULTILINE):
                errors.append(f"ABI baseline updates need `{field}: ...` under `## API compatibility` (classification: additive/breaking/tooling-only; release: patch/minor/major).")
    if requires_build_example(paths):
        build_changes = section(body, "Build changes")
        if not build_changes or not has_code_example(build_changes):
            errors.append("Build changes need a concrete Gradle or CI example under `## Build changes`.")
    visual = any(p.startswith(("demo/src/", "domposable-design-material/src/")) and p.endswith((".kt", ".css", ".html", ".png", ".svg")) for p in paths)
    if visual:
        evidence = section(body, "Visual evidence") or ""
        image_urls = re.findall(r"!\[([^\]]+)\]\((https://[^)]+)\)", evidence)
        immutable_git = r"https://raw\.githubusercontent\.com/samuolis/domposable/[0-9a-f]{40}/[^)]+\.png"
        addressed_asset = r"https://github\.com/samuolis/domposable/releases/download/visual-evidence(?:-[1-9][0-9]*)?/pr-[1-9][0-9]*-run-[1-9][0-9]*-(?:android|desktop|ios|dom|wasm)-[a-z0-9-]+-[0-9a-f]{64}\.png"
        images = [(label, url) for label, url in image_urls if re.fullmatch(immutable_git, url) or re.fullmatch(addressed_asset, url)]
        if not images:
            errors.append("Visual changes need descriptive inline PNG previews at content-addressed release asset URLs (or legacy immutable commit URLs) under `## Visual evidence`; artifact links alone are insufficient.")
    verification = section(body, "Verification")
    if not verification or not re.search(r"^\s*[-*]\s+\S", verification, re.MULTILINE):
        errors.append("Include relevant validation results under `## Verification`.")
    return errors


def validate_title(title: str) -> list[str]:
    # Ranker uses concise human-readable action titles, not mandatory commit prefixes.
    if not 12 <= len(title.strip()) <= 100 or re.search(r"\b(WIP|TODO|Untitled)\b", title, re.I):
        return ["Use a concrete action title (12–100 characters), without WIP/TODO placeholders."]
    return []


def main() -> None:
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers(dest="command", required=True)

    validate_parser = subparsers.add_parser("validate")
    validate_parser.add_argument("--body-file", required=True)
    validate_parser.add_argument("--title", required=True)
    validate_parser.add_argument("--base", required=True)
    validate_parser.add_argument("--head", required=True)

    args = parser.parse_args()
    changed_paths = collect_changed_paths(args.base, args.head)
    errors = validate_title(args.title) + validate_description(Path(args.body_file).read_text(encoding="utf-8"), changed_paths)
    if errors:
        raise SystemExit("\n".join(f"error: {error}" for error in errors))


if __name__ == "__main__":
    main()
