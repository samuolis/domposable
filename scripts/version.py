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

    def bump(self, operation):
        if operation == "patch":
            return Version(self.major, self.minor, self.patch + 1)
        if operation == "minor":
            return Version(self.major, self.minor + 1, 0)
        if operation == "major":
            return Version(self.major + 1, 0, 0)
        raise ValueError(f"Unknown bump: {operation}")

def read_version(path):
    values = {}
    for line in Path(path).read_text().splitlines():
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        key, separator, value = line.partition("=")
        if not separator or key in values:
            raise ValueError("Malformed or duplicate version property")
        values[key] = value
    if set(values) != {"VERSION", "BUILD_NUMBER"}:
        raise ValueError("version.properties must define exactly VERSION and BUILD_NUMBER")
    version = Version.parse(values["VERSION"])
    if not re.fullmatch(r"0|[1-9]\d*", values["BUILD_NUMBER"]):
        raise ValueError("BUILD_NUMBER must be a non-negative integer")
    build = int(values["BUILD_NUMBER"])
    if build > 2_100_000_000:
        raise ValueError("BUILD_NUMBER exceeds Android's versionCode limit")
    return version, build

def write_version(path, version, build):
    if build < 1 or build > 2_100_000_000:
        raise ValueError("Invalid release build number")
    Path(path).write_text(f"# Single source for Maven, Android, iOS and website versions.\nVERSION={version}\nBUILD_NUMBER={build}\n")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--file", default="version.properties")
    parser.add_argument("--bump", choices=["patch", "minor", "major"])
    parser.add_argument("--write", action="store_true")
    args = parser.parse_args()
    version, build = read_version(args.file)
    if args.bump:
        version, build = version.bump(args.bump), build + 1
    if args.write:
        if not args.bump: parser.error("--write requires --bump")
        write_version(args.file, version, build)
    print(json.dumps({"version": str(version), "build": build, "marketing": version.marketing}))
