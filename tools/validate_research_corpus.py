#!/usr/bin/env python3
"""Validate research-corpus-v1 manifest completeness, provenance, and reproducibility."""

import argparse
import hashlib
import json
import re
import sys
from pathlib import Path


REQUIRED = {"document_id", "project", "title", "source_url", "upstream_repository", "upstream_commit",
            "source_type", "retrieved_at", "license", "attribution", "content_checksum",
            "normalized_checksum", "transformation_notes", "text_path", "raw_path"}


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def validate(manifest_path, corpus_root):
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    if manifest.get("schema_version") != "research-corpus-manifest-v1":
        raise ValueError("unsupported research corpus manifest schema")
    if manifest.get("corpus_version") != "research-corpus-v1":
        raise ValueError("corpus version must be research-corpus-v1")
    documents = manifest.get("documents", [])
    if not documents:
        raise ValueError("manifest contains no documents")
    ids, raw_hashes, normalized_hashes = set(), set(), set()
    errors = []
    for index, doc in enumerate(documents, 1):
        missing = sorted(REQUIRED - set(doc))
        if missing:
            errors.append(f"document {index}: missing fields {','.join(missing)}")
            continue
        document_id = doc["document_id"]
        if document_id in ids: errors.append(f"duplicate document_id: {document_id}")
        ids.add(document_id)
        if not doc["source_url"].startswith("https://"): errors.append(f"{document_id}: invalid source_url")
        if not doc["upstream_repository"].startswith("https://github.com/"): errors.append(f"{document_id}: invalid upstream_repository")
        if not re.fullmatch(r"[0-9a-f]{40}", doc["upstream_commit"]): errors.append(f"{document_id}: upstream_commit is not a SHA-1")
        for field in ("content_checksum", "normalized_checksum"):
            if not re.fullmatch(r"[0-9a-f]{64}", doc[field]): errors.append(f"{document_id}: invalid {field}")
        for field in ("raw_path", "text_path"):
            path = (corpus_root / doc[field]).resolve()
            if corpus_root.resolve() not in path.parents: errors.append(f"{document_id}: {field} escapes corpus")
            elif not path.is_file(): errors.append(f"{document_id}: missing {field}")
            elif field == "raw_path" and sha256(path) != doc["content_checksum"]: errors.append(f"{document_id}: raw checksum mismatch")
            elif field == "text_path" and sha256(path) != doc["normalized_checksum"]: errors.append(f"{document_id}: normalized checksum mismatch")
        if doc["content_checksum"] in raw_hashes: errors.append(f"duplicate content checksum: {document_id}")
        if doc["normalized_checksum"] in normalized_hashes: errors.append(f"duplicate normalized checksum: {document_id}")
        raw_hashes.add(doc["content_checksum"]); normalized_hashes.add(doc["normalized_checksum"])
        if not doc["license"] or not doc["attribution"]: errors.append(f"{document_id}: missing license/attribution")
        if not doc["transformation_notes"]: errors.append(f"{document_id}: missing transformation notes")
    if errors: raise ValueError("; ".join(errors))
    return {"status": "valid", "documents": len(documents), "raw_checksums": len(raw_hashes), "normalized_checksums": len(normalized_hashes), "provisional": manifest.get("provisional", False)}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--corpus-root", type=Path, required=True)
    args = parser.parse_args()
    try:
        print(json.dumps(validate(args.manifest, args.corpus_root), sort_keys=True)); return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr); return 1


if __name__ == "__main__": raise SystemExit(main())
