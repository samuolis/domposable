#!/usr/bin/env python3
"""Version-file authority shared by CI, native packaging and release orchestration."""
from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
import argparse
import json
import re

VERSION_PATTERN = re.compile(r"(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-(alpha|beta|rc)(\d{2,}))?")
CHANNELS = {"alpha": 0, "beta": 1, "rc": 2, None: 3}

@dataclass(frozen=True)
class Version:
    major: int
    minor: int
    patch: int
    channel: str | None = None
    sequence: int = 0

    @classmethod
    def parse(cls, text):
        match = VERSION_PATTERN.fullmatch(text)
        if not match:
            raise ValueError(f"Invalid release version: {text!r}")
        major, minor, patch, channel, sequence = match.groups()
        return cls(int(major), int(minor), int(patch), channel, int(sequence or 0))

    def __str__(self):
        return self.marketing + (f"-{self.channel}{self.sequence:02d}" if self.channel else "")

    @property
    def marketing(self):
        return f"{self.major}.{self.minor}.{self.patch}"

    @property
    def order(self):
        return self.major, self.minor, self.patch, CHANNELS[self.channel], self.sequence

    def bump(self, operation, channel="alpha"):
        if channel not in CHANNELS or channel is None:
            raise ValueError("Choose alpha, beta or rc")
        if operation == "stable":
            if self.channel is None:
                raise ValueError("Already stable; choose patch, minor or major")
            return Version(self.major, self.minor, self.patch)
        if operation == "prerelease":
            if self.channel is None:
                return Version(self.major, self.minor, self.patch + 1, channel, 1)
            if CHANNELS[channel] < CHANNELS[self.channel]:
                raise ValueError("Cannot move a prerelease backwards to an earlier channel")
            return Version(self.major, self.minor, self.patch, channel, self.sequence + 1 if self.channel == channel else 1)
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
    parser.add_argument("--bump", choices=["prerelease", "patch", "minor", "major", "stable"])
    parser.add_argument("--channel", default="alpha", choices=["alpha", "beta", "rc"])
    parser.add_argument("--write", action="store_true")
    args = parser.parse_args()
    version, build = read_version(args.file)
    if args.bump:
        version, build = version.bump(args.bump, args.channel), build + 1
    if args.write:
        if not args.bump: parser.error("--write requires --bump")
        write_version(args.file, version, build)
    print(json.dumps({"version": str(version), "build": build, "marketing": version.marketing, "prerelease": version.channel is not None}))
