#!/usr/bin/env python3
"""Create an immutable Maven Central bundle from verified local publications."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import sys
import xml.etree.ElementTree as ET
import zipfile
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from version import read_version

ROOT = Path(__file__).resolve().parents[2]
MODULES = {"foundation", "ui", "design-material", "adaptive", "screens", "navigation3", "lifecycle-viewmodel", "ksp"}

def package(repository: Path, target: Path, signed: bool):
    version, _ = read_version(ROOT / "version.properties")
    group = repository / "io/github/samuolis/domposable"
    poms = sorted(group.glob(f"*/{version}/*.pom"))
    published = set()
    for pom in poms:
        document = ET.parse(pom).getroot()
        ns = {"m": "http://maven.apache.org/POM/4.0.0"}
        get = lambda name: document.findtext(f"m:{name}", namespaces=ns)
        artifact = get("artifactId")
        if get("version") != str(version) or get("groupId") != "io.github.samuolis.domposable":
            raise ValueError(f"Unexpected coordinates in {pom.name}")
        for field in ["name", "description", "url", "licenses", "developers", "scm"]:
            if document.find(f"m:{field}", ns) is None: raise ValueError(f"Missing POM {field}: {pom.name}")
        if artifact in {f"domposable-{name}" for name in MODULES}: published.add(artifact.removeprefix("domposable-"))
        if not list(pom.parent.glob("*-sources.jar")): raise ValueError(f"Missing sources: {artifact}")
        if not list(pom.parent.glob("*-javadoc.jar")): raise ValueError(f"Missing documentation: {artifact}")
    if published != MODULES: raise ValueError(f"Missing modules: {MODULES - published}")
    files = sorted(path for pom in poms for path in pom.parent.iterdir() if path.is_file() and not path.name.startswith("maven-metadata") and path.suffix not in {".md5", ".sha1", ".sha256", ".sha512"})
    binaries = [path for path in files if path.suffix != ".asc"]
    if signed:
        import subprocess
        for path in binaries:
            signature = path.with_name(path.name + ".asc")
            if not signature.is_file(): raise ValueError(f"Missing signature: {path.name}")
            subprocess.run(["gpg", "--batch", "--verify", str(signature), str(path)], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    target.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(target, "w", zipfile.ZIP_DEFLATED) as archive:
        for path in files:
            name = path.relative_to(repository).as_posix()
            info = zipfile.ZipInfo(name, (2026, 1, 1, 0, 0, 0)); info.compress_type = zipfile.ZIP_DEFLATED
            archive.writestr(info, path.read_bytes())
            for algorithm in ["md5", "sha1", "sha256", "sha512"]:
                checksum = hashlib.new(algorithm, path.read_bytes()).hexdigest()
                check_info = zipfile.ZipInfo(name + "." + algorithm, (2026, 1, 1, 0, 0, 0))
                archive.writestr(check_info, checksum)
    print(f"Verified {len(poms)} Maven publications")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--unsigned", action="store_true", help="CI fixture only; releases require signatures")
    args = parser.parse_args()
    package(ROOT / "build/repository", ROOT / ".release/artifacts/maven-bundle.zip", not args.unsigned)
