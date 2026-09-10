#!/usr/bin/env python3
"""Reads the JaCoCo XML report and writes a shields.io endpoint badge JSON."""

import json
import sys
import xml.etree.ElementTree as ET

XML_PATH = sys.argv[1] if len(sys.argv) > 1 else "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
OUT_PATH = sys.argv[2] if len(sys.argv) > 2 else ".github/badges/coverage.json"


def pick_color(percent: float) -> str:
    if percent >= 80:
        return "brightgreen"
    if percent >= 50:
        return "yellow"
    return "red"


def main() -> None:
    report = ET.parse(XML_PATH).getroot()
    line_counter = next(c for c in report.findall("counter") if c.get("type") == "LINE")

    covered = int(line_counter.get("covered"))
    missed = int(line_counter.get("missed"))
    total = covered + missed
    percent = round(covered / total * 100, 1) if total else 0.0

    badge = {
        "schemaVersion": 1,
        "label": "coverage",
        "message": f"{percent}%",
        "color": pick_color(percent),
    }

    with open(OUT_PATH, "w") as f:
        json.dump(badge, f)
        f.write("\n")

    print(f"Coverage: {percent}% ({covered}/{total} lines) -> {OUT_PATH}")


if __name__ == "__main__":
    main()
