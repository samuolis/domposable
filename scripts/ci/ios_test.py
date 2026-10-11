#!/usr/bin/env python3
"""Run native UI tests on an isolated simulator and remove it afterwards."""
import json
import os
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[2]
def output(*command):
    return subprocess.check_output(command, text=True)

devices = json.loads(output("xcrun", "simctl", "list", "devicetypes", "-j"))["devicetypes"]
runtimes = json.loads(output("xcrun", "simctl", "list", "runtimes", "-j"))["runtimes"]
runtime = sorted((item for item in runtimes if item.get("isAvailable") and "iOS" in item["name"]), key=lambda item: tuple(map(int, item["version"].split("."))))[-1]
available = json.loads(output("xcrun", "simctl", "list", "devices", "available", "-j"))["devices"].get(runtime["identifier"], [])
compatible = next((item for item in available if "iPad mini" in item["name"]), None) or next((item for item in available if "iPad Air 11" in item["name"]), None) or next((item for item in available if "iPad" in item["name"]), None)
device = next(item for item in devices if item["identifier"] == compatible["deviceTypeIdentifier"]) if compatible else next(item for item in devices if "iPad Air" in item["name"])
identifier = output("xcrun", "simctl", "create", f"Domposable-{os.getpid()}", device["identifier"], runtime["identifier"]).strip()
try:
    subprocess.run([
        "xcodebuild", "test", "-project", "iosApp/DomposableDemo.xcodeproj", "-scheme", "DomposableDemo",
        "-configuration", "Debug", "-destination", f"id={identifier}",
        "-derivedDataPath", f"build/ios-tests/{os.getpid()}", "-resultBundlePath", f"build/ios-tests-{os.getpid()}.xcresult",
        "CODE_SIGNING_ALLOWED=NO",
    ], cwd=root, check=True, timeout=1800)
finally:
    subprocess.run(["xcrun", "simctl", "shutdown", identifier], capture_output=True)
    subprocess.run(["xcrun", "simctl", "delete", identifier], check=True)
