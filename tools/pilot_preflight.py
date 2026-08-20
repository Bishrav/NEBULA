#!/usr/bin/env python3
"""Verify that a NEBULA API is ready for a consented pilot session."""

import argparse
import json
import sys
from datetime import datetime, timezone
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen


REQUIRED_EVENTS = {"search", "feedback", "task", "observation"}
REQUIRED_PROVENANCE = {"studyVersion", "corpusVersion", "studyWave", "querySetVersion", "codeVersion"}


def fetch_json(url):
    request = Request(url, headers={"Accept": "application/json"})
    with urlopen(request, timeout=10) as response:
        return json.loads(response.read().decode("utf-8"))


def preflight(base_url, expected=None, checked_at=None):
    base = base_url.rstrip("/")
    ready = fetch_json(base + "/health/ready")
    manifest = fetch_json(base + "/v1/research/manifest")
    checks = [
        {"name": "api_ready", "passed": ready.get("status") == "READY" and ready.get("documents", 0) > 0},
        {"name": "event_schema", "passed": REQUIRED_EVENTS.issubset(set(manifest.get("eventTypes", [])))},
        {"name": "export_formats", "passed": {"csv", "json"}.issubset(set(manifest.get("exportFormats", [])))},
        {"name": "anonymous_identity_boundary", "passed": manifest.get("privacy", {}).get("identityCollection") is False},
    ]
    for field in sorted(REQUIRED_PROVENANCE):
        checks.append({"name": f"provenance_{field}", "passed": bool(str(manifest.get(field, "")).strip())})
    for field, expected_value in (expected or {}).items():
        checks.append({"name": f"expected_{field}", "passed": manifest.get(field) == expected_value,
                       "expected": expected_value, "actual": manifest.get(field)})
    return {
        "status": "ready" if all(check["passed"] for check in checks) else "blocked",
        "checkedAt": checked_at or datetime.now(timezone.utc).replace(microsecond=0).isoformat(),
        "baseUrl": base,
        "readyResponse": ready,
        "manifest": manifest,
        "checks": checks,
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:8082")
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--study-wave")
    parser.add_argument("--corpus-version")
    parser.add_argument("--query-set-version")
    parser.add_argument("--code-version")
    parser.add_argument("--checked-at", help="fixed ISO timestamp for reproducible records")
    args = parser.parse_args()
    expected = {key: value for key, value in {
        "studyWave": args.study_wave, "corpusVersion": args.corpus_version,
        "querySetVersion": args.query_set_version, "codeVersion": args.code_version,
    }.items() if value is not None}
    try:
        result = preflight(args.base_url, expected, args.checked_at)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    except (OSError, HTTPError, URLError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1
    print(f"{result['status'].upper()}: {args.output}")
    return 0 if result["status"] == "ready" else 1


if __name__ == "__main__":
    sys.exit(main())
