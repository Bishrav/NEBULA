#!/usr/bin/env python3
"""Validate a completed, provenance-linked authority metadata review."""

import argparse
import csv
import datetime
import hashlib
import json
from pathlib import Path


FIELDS = [
    "document_id", "source_path", "project", "source_url", "current_authority_category",
    "proposed_authority", "authority_evidence", "reviewer", "reviewed_at", "review_status",
]


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def validate(manifest_path, review_path):
    expected_manifest_sha = sha256(manifest_path)
    lines = review_path.read_text(encoding="utf-8").splitlines()
    checksum_line = next((line for line in lines if line.startswith("# corpusManifestSha256=")), "")
    if checksum_line.split("=", 1)[-1] != expected_manifest_sha:
        raise ValueError("authority review does not match the supplied corpus manifest")
    rows = list(csv.DictReader((line for line in lines if line and not line.startswith("#")), delimiter="|"))
    if not rows or list(rows[0]) != FIELDS:
        raise ValueError("authority review schema mismatch")
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    expected = {document["document_id"] for document in manifest["documents"]}
    actual = {row["document_id"].strip() for row in rows}
    if actual != expected or len(rows) != len(actual):
        raise ValueError("authority review must contain exactly one row for every manifest document")
    categories = set()
    for row in rows:
        if row["review_status"].strip() != "REVIEWED":
            raise ValueError(f"document is not reviewed: {row['document_id']}")
        try:
            value = float(row["proposed_authority"])
        except ValueError as exc:
            raise ValueError(f"invalid authority value: {row['document_id']}") from exc
        if not 0.0 <= value <= 1.0:
            raise ValueError(f"authority must be between 0 and 1: {row['document_id']}")
        if not row["authority_evidence"].strip() or not row["reviewer"].strip():
            raise ValueError(f"authority evidence and reviewer are required: {row['document_id']}")
        try:
            datetime.datetime.fromisoformat(row["reviewed_at"].strip().replace("Z", "+00:00"))
        except ValueError as exc:
            raise ValueError(f"reviewed_at must be ISO-8601: {row['document_id']}") from exc
        categories.add(row["current_authority_category"].strip())
    values = [float(row["proposed_authority"]) for row in rows]
    return {
        "status": "valid",
        "documents": len(rows),
        "authorityCategories": sorted(categories),
        "distinctAuthorityValues": len(set(values)),
        "hasAuthorityVariation": len(set(values)) > 1,
        "manifestSha256": expected_manifest_sha,
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--review", type=Path, required=True)
    args = parser.parse_args()
    try:
        print(json.dumps(validate(args.manifest, args.review), sort_keys=True))
        return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}")
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
