#!/usr/bin/env python3
"""Exercise the consented NEBULA pilot flow against a local API."""

import argparse
import json
import sys
from datetime import datetime, timezone
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen

from validate_research_export import validate


def request_json(url, method="GET", headers=None, body=None):
    payload = None if body is None else json.dumps(body).encode("utf-8")
    request_headers = {"Accept": "application/json", **(headers or {})}
    if payload is not None:
        request_headers["Content-Type"] = "application/json"
    request = Request(url, data=payload, headers=request_headers, method=method)
    with urlopen(request, timeout=15) as response:
        return response.status, json.loads(response.read().decode("utf-8"))


def request_bytes(url, headers=None):
    request = Request(url, headers=headers or {})
    with urlopen(request, timeout=15) as response:
        return response.status, response.read()


def run(base_url, output_dir, session_id, task_id, checked_at=None):
    base = base_url.rstrip("/")
    headers = {"X-Session-Id": session_id, "X-Research-Consent": "true", "X-Task-Id": task_id}
    steps = []

    status, search = request_json(base + "/v1/search?" + urlencode({"q": "shard failure", "mode": "bm25", "limit": 5}), headers=headers)
    if status != 200 or not search.get("results"):
        raise ValueError("dry-run search did not return an evidence result")
    result = search["results"][0]
    steps.append("consented_search")

    status, _ = request_json(base + "/v1/research/tasks", method="POST", headers=headers,
                             body={"taskId": task_id, "action": "start"})
    if status != 201:
        raise ValueError("dry-run task start was not recorded")
    steps.append("task_start")
    status, _ = request_json(base + "/v1/research/tasks", method="POST", headers=headers,
                             body={"taskId": task_id, "action": "complete", "success": True, "durationMs": 1234})
    if status != 201:
        raise ValueError("dry-run task completion was not recorded")
    steps.append("task_complete")

    status, _ = request_json(base + "/v1/feedback", method="POST", headers=headers,
                             body={"query": "shard failure", "mode": "bm25",
                                   "documentId": result["documentId"], "sourcePath": result["sourcePath"], "useful": True})
    if status != 201:
        raise ValueError("dry-run feedback was not recorded")
    steps.append("usefulness_feedback")

    status, _ = request_json(base + "/v1/research/observations", method="POST", headers=headers,
                             body={"taskId": task_id, "confidence": 4, "note": "dry-run observation"})
    if status != 201:
        raise ValueError("dry-run observation was not recorded")
    steps.append("confidence_observation")

    status, csv_bytes = request_bytes(base + "/v1/research/export?format=csv")
    if status != 200:
        raise ValueError("dry-run export was not available")
    output_dir.mkdir(parents=True, exist_ok=True)
    export_path = output_dir / "dry-run-research.csv"
    export_path.write_bytes(csv_bytes)
    validation = validate(export_path)
    if validation["status"] != "valid":
        raise ValueError("dry-run export failed validation")
    record = {
        "status": "passed",
        "checkedAt": checked_at or datetime.now(timezone.utc).replace(microsecond=0).isoformat(),
        "baseUrl": base,
        "sessionId": session_id,
        "taskId": task_id,
        "steps": steps,
        "validation": validation,
        "export": export_path.name,
    }
    (output_dir / "dry-run-result.json").write_text(json.dumps(record, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return record


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:8082")
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--session-id", default="dry-run-session")
    parser.add_argument("--task-id", default="shard-failure")
    parser.add_argument("--checked-at")
    args = parser.parse_args()
    try:
        record = run(args.base_url, args.output_dir, args.session_id, args.task_id, args.checked_at)
    except (OSError, HTTPError, URLError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1
    print(f"PASSED: {record['export']}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
