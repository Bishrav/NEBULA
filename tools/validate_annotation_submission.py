#!/usr/bin/env python3
"""Validate a returned blinded PSV package and create a protected canonical export."""

import argparse
import csv
import io
import json
import sys
import zipfile
from datetime import datetime
from pathlib import Path

PACKET_FIELDS = ["annotation_query_id", "query_text", "document_id", "document_title", "document_excerpt", "relevance_grade_0_3", "uncertainty", "evidence_note", "annotated_at"]
CANONICAL_FIELDS = ["annotator_code", "query_id", "document_id", "grade", "uncertainty", "evidence_note", "annotated_at"]


def psv_texts(path):
    if path.is_dir():
        return [(item.name, item.read_text(encoding="utf-8")) for item in sorted(path.glob("*.psv"))]
    with zipfile.ZipFile(path) as archive:
        return [(name, archive.read(name).decode("utf-8")) for name in sorted(archive.namelist()) if name.endswith(".psv")]


def load_rows(path):
    rows = []
    for name, text in psv_texts(path):
        reader = csv.DictReader(io.StringIO(text), delimiter="|")
        if reader.fieldnames != PACKET_FIELDS:
            raise ValueError(f"{name}: packet schema mismatch")
        rows.extend((name, row) for row in reader)
    if not rows:
        raise ValueError("submission contains no PSV annotation rows")
    return rows


def load_key(path):
    with path.open(encoding="utf-8", newline="") as handle:
        reader = csv.DictReader(handle, delimiter="|")
        if reader.fieldnames != ["annotation_query_id", "original_query_id"]:
            raise ValueError("private query key schema mismatch")
        mapping = {row["annotation_query_id"]: row["original_query_id"] for row in reader}
    if not mapping or len(mapping) != len(set(mapping)):
        raise ValueError("private query key is empty or duplicated")
    return mapping


def validate(submission, packet, key, annotator, output):
    allowed = {(row["annotation_query_id"], row["document_id"]) for _, row in load_rows(packet)}
    mapping = load_key(key)
    seen, canonical = set(), []
    for name, row in load_rows(submission):
        query_id, document_id = row["annotation_query_id"].strip(), row["document_id"].strip()
        if (query_id, document_id) not in allowed:
            raise ValueError(f"{name}: pair is not in the frozen annotator package")
        if query_id not in mapping:
            raise ValueError(f"{name}: annotation query ID is not in the private key")
        if (query_id, document_id) in seen:
            raise ValueError(f"{name}: duplicate annotation for {query_id}/{document_id}")
        seen.add((query_id, document_id))
        try:
            grade = int(row["relevance_grade_0_3"])
        except ValueError as exc:
            raise ValueError(f"{name}: relevance grade must be 0..3") from exc
        if grade not in range(4):
            raise ValueError(f"{name}: relevance grade must be 0..3")
        uncertainty = row["uncertainty"].strip().lower()
        if uncertainty not in {"true", "false", "yes", "no"}:
            raise ValueError(f"{name}: uncertainty must be boolean")
        if not row["evidence_note"].strip():
            raise ValueError(f"{name}: evidence note is required")
        try:
            datetime.fromisoformat(row["annotated_at"].strip().replace("Z", "+00:00"))
        except ValueError as exc:
            raise ValueError(f"{name}: annotated_at must be ISO-8601") from exc
        canonical.append({"annotator_code": annotator, "query_id": mapping[query_id], "document_id": document_id, "grade": str(grade), "uncertainty": "true" if uncertainty in {"true", "yes"} else "false", "evidence_note": row["evidence_note"].strip(), "annotated_at": row["annotated_at"].strip()})
    if len(seen) != len(allowed):
        raise ValueError(f"submission is incomplete: received {len(seen)} of {len(allowed)} pairs")
    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=CANONICAL_FIELDS, delimiter="|", lineterminator="\n")
        writer.writeheader(); writer.writerows(canonical)
    return {"status": "valid", "annotatorCode": annotator, "rows": len(canonical), "humanGenerated": True}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--submission", type=Path, required=True); parser.add_argument("--packet", type=Path, required=True)
    parser.add_argument("--private-key", type=Path, required=True); parser.add_argument("--annotator", required=True); parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    try:
        print(json.dumps(validate(args.submission, args.packet, args.private_key, args.annotator, args.output), indent=2, sort_keys=True))
        return 0
    except (OSError, ValueError, zipfile.BadZipFile) as exc:
        print(f"ERROR: {exc}", file=sys.stderr); return 1


if __name__ == "__main__":
    raise SystemExit(main())
