#!/usr/bin/env python3
"""Capture a NEBULA study-wave bundle and generate its validated PMF report."""

import argparse
import json
import sys
from datetime import datetime, timezone
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

from generate_pmf_report import build_report
from validate_research_export import validate


def fetch(url):
    request = Request(url, headers={"Accept": "application/json, text/csv"})
    with urlopen(request, timeout=20) as response:
        return response.read()


def capture(base_url, output_dir, generated_at=None):
    base = base_url.rstrip("/")
    output_dir.mkdir(parents=True, exist_ok=True)
    csv_path = output_dir / "nebula-research.csv"
    json_path = output_dir / "nebula-research.json"
    manifest_path = output_dir / "nebula-research-manifest.json"
    validation_path = output_dir / "validation.json"
    report_path = output_dir / "pmf-report.md"

    csv_bytes = fetch(base + "/v1/research/export?format=csv")
    json_bytes = fetch(base + "/v1/research/export?format=json")
    manifest_bytes = fetch(base + "/v1/research/manifest")
    manifest = json.loads(manifest_bytes.decode("utf-8"))
    required_manifest = {"studyVersion", "corpusVersion", "studyWave", "querySetVersion", "codeVersion"}
    missing = sorted(required_manifest - set(manifest))
    if missing:
        raise ValueError("manifest is missing provenance fields: " + ", ".join(missing))

    csv_path.write_bytes(csv_bytes)
    json_path.write_bytes(json_bytes)
    manifest_path.write_bytes(manifest_bytes)
    validation = validate(csv_path)
    validation_path.write_text(json.dumps(validation, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    if validation["status"] != "valid":
        raise ValueError("captured CSV failed validation; see " + str(validation_path))

    report = build_report(csv_path, manifest_path, generated_at)
    report_path.write_text(report, encoding="utf-8")
    capture_record = {
        "capturedAt": generated_at or datetime.now(timezone.utc).replace(microsecond=0).isoformat(),
        "baseUrl": base,
        "files": [path.name for path in (csv_path, json_path, manifest_path, validation_path, report_path)],
        "provenance": {field: manifest[field] for field in sorted(required_manifest)},
    }
    (output_dir / "capture.json").write_text(json.dumps(capture_record, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return capture_record


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:8082")
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--captured-at", help="fixed ISO timestamp for reproducible bundle metadata")
    args = parser.parse_args()
    try:
        result = capture(args.base_url, args.output_dir, args.captured_at)
    except (OSError, HTTPError, URLError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1
    print(f"CAPTURED: {result['provenance']['studyWave']} -> {args.output_dir}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
