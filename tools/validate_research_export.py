#!/usr/bin/env python3
"""Validate a NEBULA CSV research export before analysis or publication."""

import argparse
import csv
import json
import sys
from collections import Counter, defaultdict
from pathlib import Path


REQUIRED_COLUMNS = {
    "type", "sessionId", "timestamp", "query", "mode", "results",
    "latencyNanos", "documentId", "sourcePath", "useful", "taskId",
    "action", "durationMs", "success", "confidence", "note",
}
EVENT_TYPES = {"search", "feedback", "task", "observation"}


def add_error(errors, row_number, message):
    errors.append(f"row {row_number}: {message}")


def required_text(row, field, row_number, errors):
    value = row.get(field, "").strip()
    if not value:
        add_error(errors, row_number, f"{field} is required")
    return value


def integer(row, field, row_number, errors, required=False, minimum=None):
    value = row.get(field, "").strip()
    if not value:
        if required:
            add_error(errors, row_number, f"{field} is required")
        return None
    try:
        parsed = int(value)
    except ValueError:
        add_error(errors, row_number, f"{field} must be an integer")
        return None
    if minimum is not None and parsed < minimum:
        add_error(errors, row_number, f"{field} must be >= {minimum}")
    return parsed


def boolean(row, field, row_number, errors, required=False):
    value = row.get(field, "").strip().lower()
    if not value:
        if required:
            add_error(errors, row_number, f"{field} is required")
        return None
    if value not in {"true", "false"}:
        add_error(errors, row_number, f"{field} must be true or false")
        return None
    return value == "true"


def validate(path):
    errors = []
    warnings = []
    event_counts = Counter()
    sessions = defaultdict(list)
    task_keys = set()
    task_starts = set()
    task_completions = set()

    try:
        handle = path.open(newline="", encoding="utf-8-sig")
    except OSError as exc:
        return {"status": "invalid", "errors": [str(exc)], "warnings": []}

    with handle:
        reader = csv.DictReader(handle)
        columns = set(reader.fieldnames or [])
        missing = sorted(REQUIRED_COLUMNS - columns)
        if missing:
            errors.append("missing columns: " + ", ".join(missing))
        if reader.fieldnames and len(reader.fieldnames) != len(columns):
            errors.append("duplicate column names are not allowed")

        for row_number, row in enumerate(reader, start=2):
            event_type = required_text(row, "type", row_number, errors)
            session_id = required_text(row, "sessionId", row_number, errors)
            timestamp = integer(row, "timestamp", row_number, errors, required=True, minimum=1)
            if event_type not in EVENT_TYPES:
                add_error(errors, row_number, f"unknown event type {event_type!r}")
            else:
                event_counts[event_type] += 1
            if session_id:
                sessions[session_id].append(row_number)
            if timestamp is not None and event_type in EVENT_TYPES:
                if event_type == "search":
                    required_text(row, "query", row_number, errors)
                    required_text(row, "mode", row_number, errors)
                    integer(row, "results", row_number, errors, required=True, minimum=0)
                    integer(row, "latencyNanos", row_number, errors, required=True, minimum=0)
                elif event_type == "feedback":
                    required_text(row, "query", row_number, errors)
                    required_text(row, "mode", row_number, errors)
                    required_text(row, "documentId", row_number, errors)
                    required_text(row, "sourcePath", row_number, errors)
                    boolean(row, "useful", row_number, errors, required=True)
                elif event_type == "task":
                    task_id = required_text(row, "taskId", row_number, errors)
                    action = required_text(row, "action", row_number, errors).lower()
                    if action not in {"start", "complete"}:
                        add_error(errors, row_number, "action must be start or complete")
                    task_key = (session_id, task_id)
                    duplicate_key = (session_id, task_id, action, timestamp)
                    if duplicate_key in task_keys:
                        add_error(errors, row_number, "duplicate task event")
                    task_keys.add(duplicate_key)
                    if action == "start":
                        task_starts.add(task_key)
                    if action == "complete":
                        task_completions.add(task_key)
                        integer(row, "durationMs", row_number, errors, required=True, minimum=0)
                        boolean(row, "success", row_number, errors, required=True)
                elif event_type == "observation":
                    required_text(row, "taskId", row_number, errors)
                    integer(row, "confidence", row_number, errors, required=True, minimum=1)
                    confidence = row.get("confidence", "").strip()
                    if confidence.isdigit() and int(confidence) > 5:
                        add_error(errors, row_number, "confidence must be between 1 and 5")

    for session_id, row_numbers in sessions.items():
        if row_numbers != sorted(row_numbers):
            warnings.append(f"session {session_id!r} is not ordered by export row")
    for task_key in sorted(task_completions - task_starts):
        errors.append(f"task completion has no matching start: {task_key[0]}/{task_key[1]}")

    return {
        "status": "valid" if not errors else "invalid",
        "file": str(path),
        "rows": sum(event_counts.values()),
        "sessions": len(sessions),
        "eventCounts": dict(sorted(event_counts.items())),
        "errors": errors,
        "warnings": warnings,
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("csv_file", type=Path)
    parser.add_argument("--json", action="store_true", dest="as_json", help="emit machine-readable output")
    args = parser.parse_args()
    result = validate(args.csv_file)
    if args.as_json:
        print(json.dumps(result, indent=2, sort_keys=True))
    else:
        print(f"{result['status'].upper()}: {result.get('file', args.csv_file)}")
        print(f"rows={result.get('rows', 0)} sessions={result.get('sessions', 0)} events={result.get('eventCounts', {})}")
        for message in result["errors"]:
            print(f"ERROR: {message}")
        for message in result["warnings"]:
            print(f"WARNING: {message}")
    return 0 if result["status"] == "valid" else 1


if __name__ == "__main__":
    sys.exit(main())
