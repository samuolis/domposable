"""Summarize Kover's JaCoCo-compatible XML without treating missing data as coverage."""

import argparse
import json
from pathlib import Path
import xml.etree.ElementTree as ET


def counter(element, kind, required=False):
    value = element.find(f"counter[@type='{kind}']")
    if value is None:
        if required:
            raise ValueError(f"Coverage report has no {kind} counter")
        return {"covered": 0, "missed": 0, "percent": None}
    covered = int(value.attrib["covered"])
    missed = int(value.attrib["missed"])
    if min(covered, missed) < 0:
        raise ValueError("Coverage counters must be non-negative")
    total = covered + missed
    return {
        "covered": covered,
        "missed": missed,
        "percent": round(100 * covered / total, 2) if total else None,
    }


def read_report(path):
    root = ET.parse(path).getroot()
    if root.tag != "report":
        raise ValueError("Expected a JaCoCo-compatible coverage report")
    lines = counter(root, "LINE", required=True)
    if lines["percent"] is None:
        raise ValueError("Coverage report contains no measured source lines")
    return {
        "scope": "JVM and Android host tests; JS, Wasm and iOS are not instrumented",
        "line": lines,
        "branch": counter(root, "BRANCH"),
        "packages": [
            {
                "name": package.attrib["name"].replace("/", "."),
                "line": counter(package, "LINE", required=True),
                "branch": counter(package, "BRANCH"),
            }
            for package in sorted(root.findall("package"), key=lambda item: item.attrib["name"])
        ],
    }


def markdown(report):
    def percentage(value):
        return "n/a" if value["percent"] is None else f"{value['percent']:.2f}%"

    rows = [
        "## Test coverage",
        "",
        report["scope"],
        "",
        "| Package | Lines | Branches |",
        "| --- | ---: | ---: |",
        f"| **Total** | **{percentage(report['line'])}** | **{percentage(report['branch'])}** |",
    ]
    for package in report["packages"]:
        rows.append(f"| {package['name']} | {percentage(package['line'])} | {percentage(package['branch'])} |")
    rows.extend(["", "Download `quality-and-coverage` for the HTML report, XML and lint findings.", ""])
    return "\n".join(rows)


def verify(report, policy):
    def check(name, values, floors):
        for kind, floor in floors.items():
            if kind not in ("line", "branch") or not isinstance(floor, (int, float)) or not 0 < floor <= 100:
                raise ValueError(f"Invalid coverage floor: {name}/{kind}")
            counts = values[kind]
            total = counts["covered"] + counts["missed"]
            result = 100 * counts["covered"] / total if total else None
            if result is None or result < floor:
                raise ValueError(f"{name}: {kind} coverage {result} is below {floor}%")

    check("Total", report, policy["total"])
    packages = {package["name"]: package for package in report["packages"]}
    for name, floors in policy["packages"].items():
        if name not in packages:
            raise ValueError(f"Required coverage package is missing: {name}")
        check(name, packages[name], floors)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("report", type=Path)
    parser.add_argument("--summary", type=Path)
    parser.add_argument("--verify", type=Path, help="Enforce the specified coverage policy")
    args = parser.parse_args()
    report = read_report(args.report)
    args.report.with_name("coverage-summary.json").write_text(json.dumps(report, indent=2) + "\n")
    if args.verify:
        verify(report, json.loads(args.verify.read_text()))
    result = markdown(report)
    print(result)
    if args.summary:
        with args.summary.open("a") as summary:
            summary.write(result)


if __name__ == "__main__":
    main()
