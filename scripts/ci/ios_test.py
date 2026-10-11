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
runtime = sorted((item for item in runtimes if item.get("isAvailable") and item["version"] == "26.5" and "iOS" in item["name"]), key=lambda item: tuple(map(int, item["version"].split("."))))[-1]
available = json.loads(output("xcrun", "simctl", "list", "devices", "available", "-j"))["devices"].get(runtime["identifier"], [])
device = next(item for item in devices if item["identifier"] == "com.apple.CoreSimulator.SimDeviceType.iPad-mini-A17-Pro")
identifier = output("xcrun", "simctl", "create", f"Domposable-{os.getpid()}", device["identifier"], runtime["identifier"]).strip()
result = root / f"build/ios-tests-{os.getpid()}.xcresult"
try:
    subprocess.run(["xcrun", "simctl", "boot", identifier], check=True)
    subprocess.run(["xcrun", "simctl", "bootstatus", identifier, "-b"], check=True, timeout=600)
    subprocess.run(["xcrun", "simctl", "status_bar", identifier, "override", "--time", "2026-10-11T09:41:00.000Z", "--batteryState", "charged", "--batteryLevel", "100", "--wifiMode", "active", "--wifiBars", "3"], check=True)
    subprocess.run([
        "xcodebuild", "test", "-project", "iosApp/DomposableDemo.xcodeproj", "-scheme", "DomposableDemo",
        "-configuration", "Debug", "-destination", f"id={identifier}",
        "-derivedDataPath", f"build/ios-tests/{os.getpid()}", "-resultBundlePath", str(result),
        "CODE_SIGNING_ALLOWED=NO",
    ], cwd=root, check=True, timeout=1800)
finally:
    try:
        if result.exists():
            exported = root / f"build/ios-attachments-{os.getpid()}"
            subprocess.run(["xcrun", "xcresulttool", "export", "attachments", "--path", str(result), "--output-path", str(exported)], check=True)
            destination = root / "build/screenshots/ios"
            destination.mkdir(parents=True, exist_ok=True)
            import shutil
            for test in json.loads((exported/"manifest.json").read_text()):
                for attachment in test["attachments"]:
                    name = attachment["suggestedHumanReadableName"]
                    if name.startswith("golden-"):
                        name = name.removeprefix("golden-").removesuffix(".png")
                        # Xcode can append a timestamp to human-readable names.
                        name = name.split("_")[0]
                        if name in set(json.loads((root/"screenshots/manifest.json").read_text())["ios"]):
                            shutil.copy2(exported/attachment["exportedFileName"], destination/f"{name}.png")
    finally:
        subprocess.run(["xcrun", "simctl", "shutdown", identifier], capture_output=True)
        subprocess.run(["xcrun", "simctl", "delete", identifier], check=True)
