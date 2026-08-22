#!/usr/bin/env python3
"""Validate anonymous annotation exports against blinded packet contents."""

import argparse
import csv
import json
import re
import sys
from datetime import datetime
from pathlib import Path


def packet_keys(packet_path):
    packet = json.loads(packet_path.read_text(encoding="utf-8"))
    keys = {(item["queryId"], candidate["documentId"]) for item in packet["items"] for candidate in item["candidates"]}
    return packet, keys


def validate(annotation_path, packet_path, expected_annotator=None):
    packet, allowed = packet_keys(packet_path)
    seen, rows = set(), []
    with annotation_path.open(encoding="utf-8", newline="") as handle:
        reader = csv.DictReader(handle, delimiter="|")
        required = ["annotator_code", "query_id", "document_id", "grade", "uncertainty", "evidence_note", "annotated_at"]
        if reader.fieldnames != required: raise ValueError("annotation export schema mismatch")
        for number, row in enumerate(reader, 2):
            code, query, document = row["annotator_code"].strip(), row["query_id"].strip(), row["document_id"].strip()
            if expected_annotator and code != expected_annotator: raise ValueError(f"line {number}: annotator code mismatch")
            if (query, document) not in allowed: raise ValueError(f"line {number}: item not in blinded packet")
            key = (code, query, document)
            if key in seen: raise ValueError(f"line {number}: duplicate annotation")
            seen.add(key)
            try: grade = int(row["grade"])
            except ValueError as exc: raise ValueError(f"line {number}: grade must be 0..3") from exc
            if grade not in range(4): raise ValueError(f"line {number}: grade must be 0..3")
            if row["uncertainty"].lower() not in {"true", "false", "yes", "no"}: raise ValueError(f"line {number}: uncertainty must be boolean")
            if not row["evidence_note"].strip(): raise ValueError(f"line {number}: evidence note required")
            try: datetime.fromisoformat(row["annotated_at"].replace("Z", "+00:00"))
            except ValueError as exc: raise ValueError(f"line {number}: invalid annotated_at") from exc
            rows.append(row)
    if not rows: raise ValueError("annotation export contains no records")
    if not packet.get("annotatorCode"): raise ValueError("packet is missing anonymous annotator code")
    return {"status": "valid", "rows": len(rows), "annotatorCode": packet["annotatorCode"], "packetItems": len(allowed), "humanGenerated": True}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("annotations", type=Path); parser.add_argument("--packet", type=Path, required=True); parser.add_argument("--annotator", required=True); parser.add_argument("--json", type=Path)
    args = parser.parse_args()
    try:
        result = validate(args.annotations, args.packet, args.annotator); payload = json.dumps(result, indent=2, sort_keys=True) + "\n"
        if args.json: args.json.parent.mkdir(parents=True, exist_ok=True); args.json.write_text(payload, encoding="utf-8")
        print(payload, end=""); return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc: print(f"ERROR: {exc}", file=sys.stderr); return 1


if __name__ == "__main__": raise SystemExit(main())
