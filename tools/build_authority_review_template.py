#!/usr/bin/env python3
"""Create a provenance-preserving, blank authority-review template."""

import argparse
import csv
import hashlib
import json
from pathlib import Path


FIELDS = [
    "document_id", "source_path", "project", "source_url", "current_authority_category",
    "proposed_authority", "authority_evidence", "reviewer", "reviewed_at", "review_status",
]


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def build(manifest_path, output_path):
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    rows = []
    for document in manifest["documents"]:
        rows.append({
            "document_id": document["document_id"],
            "source_path": document["text_path"].replace("\\", "/").removeprefix("normalized/"),
            "project": document["project"],
            "source_url": document["source_url"],
            "current_authority_category": document.get("authority_category", ""),
            "proposed_authority": "",
            "authority_evidence": "",
            "reviewer": "",
            "reviewed_at": "",
            "review_status": "UNREVIEWED",
        })
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with output_path.open("w", encoding="utf-8", newline="") as handle:
        handle.write(f"# corpusManifestSha256={sha256(manifest_path)}\n")
        handle.write("# Authority is an observable source-priority input, not a truth label.\n")
        writer = csv.DictWriter(handle, fieldnames=FIELDS, delimiter="|", lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)
    return {"documents": len(rows), "manifestSha256": sha256(manifest_path), "status": "REVIEW_REQUIRED"}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    print(json.dumps(build(args.manifest, args.output), sort_keys=True))


if __name__ == "__main__":
    main()
