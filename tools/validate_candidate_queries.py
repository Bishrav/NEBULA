#!/usr/bin/env python3
"""Validate candidate engineering information needs without creating qrels."""

import argparse
import csv
import json
import re
import sys
from pathlib import Path


FIELDS = ["query_id", "query_text", "category", "project", "difficulty", "freshness_sensitive", "authority_sensitive",
          "exact_identifier_dependency", "evidence_required", "source_generation_method", "grounding_document_id",
          "grounding_excerpt_checksum", "review_status"]
ALLOWED_CATEGORIES = {"debugging", "deployment", "configuration", "networking", "security", "observability",
                      "database administration", "performance", "migration", "compatibility", "architecture",
                      "failure recovery", "API usage"}


def validate(path, manifest=None):
    with path.open(encoding="utf-8", newline="") as handle:
        reader = csv.DictReader(handle, delimiter="|")
        if reader.fieldnames != FIELDS: raise ValueError("candidate query schema mismatch")
        rows = list(reader)
    ids, texts, errors = set(), set(), []
    manifest_docs = {doc["document_id"]: doc for doc in (manifest or {}).get("documents", [])}
    for row in rows:
        qid, query = row["query_id"], re.sub(r"\s+", " ", row["query_text"]).strip()
        if not qid or qid in ids: errors.append(f"duplicate or empty query_id: {qid}")
        if not query or len(query) < 30 or not query.endswith("?"): errors.append(f"malformed or vague query: {qid}")
        if query.lower() in texts: errors.append(f"duplicate query text: {qid}")
        ids.add(qid); texts.add(query.lower())
        if row["category"] not in ALLOWED_CATEGORIES: errors.append(f"invalid category: {qid}")
        if row["review_status"] != "NEEDS_HUMAN_REVIEW": errors.append(f"candidate was prematurely approved: {qid}")
        if row["grounding_document_id"] and manifest_docs and row["grounding_document_id"] not in manifest_docs: errors.append(f"unknown grounding document: {qid}")
        if query.lower() == row["query_text"].strip().lower():
            pass
        if re.search(r"^(what is|documentation for|about)\s+[^?]+\??$", query, re.I): errors.append(f"title-like query: {qid}")
    if errors: raise ValueError("; ".join(errors))
    return {"status": "valid", "candidates": len(rows), "categories": sorted({row["category"] for row in rows}), "qrels_created": False}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--queries", type=Path, required=True)
    parser.add_argument("--manifest", type=Path)
    args = parser.parse_args()
    try:
        manifest = json.loads(args.manifest.read_text(encoding="utf-8")) if args.manifest else None
        print(json.dumps(validate(args.queries, manifest), sort_keys=True)); return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr); return 1


if __name__ == "__main__": raise SystemExit(main())
