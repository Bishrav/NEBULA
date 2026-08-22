#!/usr/bin/env python3
"""Validate provenance, integrity, and links for an admitted public corpus."""

import argparse
import hashlib
import json
import re
import sys
from datetime import datetime
from pathlib import Path


HEADER = [
    "document_id", "title", "source", "source_type", "project", "created_at", "updated_at",
    "version", "authority_category", "source_url", "graph_links", "text_path", "sha256",
    "license", "attribution",
]
ISO = re.compile(r"^\d{4}-\d{2}-\d{2}(?:T.*)?$")


def sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def parse_manifest(path):
    rows = []
    for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        row = line.strip()
        if not row or row.startswith("#"):
            continue
        fields = [part.strip() for part in row.split("|")]
        if not rows and fields == HEADER:
            continue
        if len(fields) != len(HEADER):
            raise ValueError(f"line {number}: expected {len(HEADER)} fields, got {len(fields)}")
        rows.append(dict(zip(HEADER, fields)))
    if not rows:
        raise ValueError("manifest contains no document rows")
    return rows


def validate(manifest_path, corpus_dir):
    rows = parse_manifest(manifest_path)
    errors = []
    ids = set()
    paths = set()
    for row in rows:
        document_id = row["document_id"]
        if not document_id or document_id in ids:
            errors.append(f"duplicate or empty document_id: {document_id}")
        ids.add(document_id)
        text_path = row["text_path"]
        if not text_path or text_path in paths:
            errors.append(f"duplicate or empty text_path: {text_path}")
        paths.add(text_path)
        if not row["title"] or not row["source"] or not row["source_type"] or not row["project"]:
            errors.append(f"{document_id}: missing required identity metadata")
        if not row["version"] or not row["authority_category"]:
            errors.append(f"{document_id}: missing version or authority category")
        if not row["source_url"].startswith("https://"):
            errors.append(f"{document_id}: source_url must use https")
        if not row["license"] or not row["attribution"]:
            errors.append(f"{document_id}: license and attribution are required")
        for field in ("created_at", "updated_at"):
            if not ISO.match(row[field]):
                errors.append(f"{document_id}: {field} must be ISO-8601")
            else:
                try:
                    datetime.fromisoformat(row[field].replace("Z", "+00:00"))
                except ValueError:
                    errors.append(f"{document_id}: invalid {field}")
        resolved = (corpus_dir / text_path).resolve()
        if corpus_dir.resolve() not in resolved.parents:
            errors.append(f"{document_id}: text_path escapes corpus directory")
        elif not resolved.is_file():
            errors.append(f"{document_id}: missing text file {text_path}")
        elif row["sha256"] != sha256(resolved):
            errors.append(f"{document_id}: SHA-256 mismatch")
        if row["sha256"] and not re.fullmatch(r"[0-9a-fA-F]{64}", row["sha256"]):
            errors.append(f"{document_id}: sha256 must be 64 hexadecimal characters")

    for row in rows:
        for target in filter(None, (part.strip() for part in row["graph_links"].split(";"))):
            if target not in ids:
                errors.append(f"{row['document_id']}: graph link target not in manifest: {target}")
    if errors:
        raise ValueError("; ".join(errors))
    return {"status": "valid", "documents": len(rows), "manifest": str(manifest_path)}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--corpus-dir", type=Path, required=True)
    args = parser.parse_args()
    try:
        print(json.dumps(validate(args.manifest, args.corpus_dir), sort_keys=True))
        return 0
    except (OSError, ValueError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
