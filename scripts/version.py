#!/usr/bin/env python3
"""Version-file authority shared by CI, native packaging and release orchestration."""
from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
import argparse
import json
import re

VERSION_PATTERN = re.compile(r"(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)")

@dataclass(frozen=True)
class Version:
    major: int
    minor: int
    patch: int

    @classmethod
    def parse(cls, text):
        match = VERSION_PATTERN.fullmatch(text)
        if not match:
            raise ValueError(f"Invalid release version: {text!r}")
        return cls(*(int(value) for value in match.groups()))

    def __str__(self):
        return self.marketing

    @property
    def marketing(self):
        return f"{self.major}.{self.minor}.{self.patch}"

    @property
    def order(self):
        return self.major, self.minor, self.patch

    @property
    def android_code(self):
        if self.minor >= 1_000 or self.patch >= 1_000:
            raise ValueError("Android version codes require minor and patch below 1000")
        code = self.major * 1_000_000 + self.minor * 1_000 + self.patch
        if code > 2_100_000_000:
            raise ValueError("Version exceeds Android's versionCode limit")
        return code

    def bump(self, operation):
        if operation == "patch":
            return Version(self.major, self.minor, self.patch + 1)
        if operation == "minor":
            return Version(self.major, self.minor + 1, 0)
        if operation == "major":
            return Version(self.major + 1, 0, 0)
        raise ValueError(f"Unknown bump: {operation}")

def _read_properties(path):
    values = {}
    for line in Path(path).read_text().splitlines():
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        key, separator, value = line.partition("=")
        if not separator or key in values:
            raise ValueError("Malformed or duplicate version property")
        values[key] = value
    if set(values) not in ({"VERSION"}, {"VERSION", "BUILD_NUMBER"}):
        raise ValueError("version.properties must define VERSION; only legacy BUILD_NUMBER is also accepted")
    if "BUILD_NUMBER" in values:
        if not re.fullmatch(r"0|[1-9]\d*", values["BUILD_NUMBER"]):
            raise ValueError("Legacy BUILD_NUMBER must be a non-negative integer")
        if int(values["BUILD_NUMBER"]) > 2_100_000_000:
            raise ValueError("Legacy BUILD_NUMBER exceeds Android's versionCode limit")
    return values

def read_version(path):
    version = Version.parse(_read_properties(path)["VERSION"])
    return version, version.android_code

def read_checkpoint_version(path):
    """Recover historical immutable candidates without reinterpreting their APK codes."""
    values = _read_properties(path)
    version = Version.parse(values["VERSION"])
    build = int(values["BUILD_NUMBER"]) if "BUILD_NUMBER" in values else version.android_code
    return version, build

def write_version(path, version):
    if version.android_code < 1:
        raise ValueError("The unpublished 0.0.0 bootstrap is not a release")
    Path(path).write_text(f"# Single source for Maven, Android, iOS and website versions.\nVERSION={version}\n")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--file", default="version.properties")
    parser.add_argument("--bump", choices=["patch", "minor", "major"])
    parser.add_argument("--write", action="store_true")
    args = parser.parse_args()
    version, build = read_version(args.file)
    if args.bump:
        version = version.bump(args.bump)
        build = version.android_code
    if args.write:
        if not args.bump: parser.error("--write requires --bump")
        write_version(args.file, version)
    print(json.dumps({"version": str(version), "build": build, "marketing": version.marketing}))
