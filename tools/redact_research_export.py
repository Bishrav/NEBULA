#!/usr/bin/env python3
"""Create a publication-safe derivative of a NEBULA CSV export."""

import argparse
import csv
import hashlib
import json
import os
import sys
from pathlib import Path

from validate_research_export import validate


def pseudonym(value, salt):
    digest = hashlib.sha256((salt + "\0" + value).encode("utf-8")).hexdigest()[:16]
    return "hash_" + digest


def redact(input_path, output_path, record_path, salt, hash_queries=False, hash_sources=False):
    validation = validate(input_path)
    if validation["status"] != "valid":
        raise ValueError("source export failed validation; redact the raw export only after fixing the source")
    with input_path.open(newline="", encoding="utf-8-sig") as source:
        reader = csv.DictReader(source)
        rows = list(reader)
        fieldnames = reader.fieldnames or []
    for row in rows:
        row["sessionId"] = pseudonym(row["sessionId"] or "anonymous", salt)
        row["note"] = "[redacted]" if (row["note"] or "").strip() else ""
        if hash_queries and (row["query"] or "").strip():
            row["query"] = pseudonym(row["query"], salt)
        if hash_sources:
            if (row["documentId"] or "").strip():
                row["documentId"] = pseudonym(row["documentId"], salt)
            if (row["sourcePath"] or "").strip():
                row["sourcePath"] = pseudonym(row["sourcePath"], salt)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with output_path.open("w", newline="", encoding="utf-8") as target:
        writer = csv.DictWriter(target, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)
    output_validation = validate(output_path)
    if output_validation["status"] != "valid":
        raise ValueError("redacted derivative failed validation")
    record = {
        "status": "valid",
        "source": str(input_path),
        "output": str(output_path),
        "rows": len(rows),
        "transform": {
            "sessionIds": "salted-sha256-prefix",
            "notes": "removed",
            "queries": "salted-sha256-prefix" if hash_queries else "preserved",
            "sources": "salted-sha256-prefix" if hash_sources else "preserved",
        },
        "validation": output_validation,
    }
    record_path.parent.mkdir(parents=True, exist_ok=True)
    record_path.write_text(json.dumps(record, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return record


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input_csv", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--record", type=Path, required=True)
    parser.add_argument("--hash-queries", action="store_true")
    parser.add_argument("--hash-sources", action="store_true")
    parser.add_argument("--salt-env", default="NEBULA_REDACTION_SALT")
    args = parser.parse_args()
    salt = os.environ.get(args.salt_env)
    if not salt:
        print(f"ERROR: set a private salt in {args.salt_env}; never commit the salt", file=sys.stderr)
        return 1
    try:
        redact(args.input_csv, args.output, args.record, salt, args.hash_queries, args.hash_sources)
    except (OSError, ValueError, csv.Error) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1
    print(f"WROTE: {args.output}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
