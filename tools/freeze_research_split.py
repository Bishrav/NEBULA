#!/usr/bin/env python3
"""Create an immutable, stratified 60/20/20 research split after qrels exist."""

import argparse
import csv
import hashlib
import json
import random
import sys
from collections import defaultdict
from pathlib import Path


SPLITS = ("development", "validation", "test")


def sha256(path): return hashlib.sha256(path.read_bytes()).hexdigest()


def load_queries(path):
    with path.open(encoding="utf-8", newline="") as handle:
        reader = csv.DictReader(handle, delimiter="|")
        required = {"query_id", "query_text", "category", "project", "difficulty", "freshness_sensitive", "review_status"}
        if not required.issubset(set(reader.fieldnames or [])): raise ValueError("reviewed query schema is incomplete")
        rows = list(reader)
    if len({row["query_id"] for row in rows}) != len(rows): raise ValueError("duplicate query IDs")
    if any(row["review_status"] != "APPROVED" for row in rows): raise ValueError("all queries must be human-approved before freezing")
    return rows


def load_qrels(path, query_ids):
    with path.open(encoding="utf-8", newline="") as handle:
        reader = csv.DictReader(handle, delimiter="|")
        if reader.fieldnames != ["query_id", "document_id", "relevance_grade"]: raise ValueError("qrels schema mismatch")
        rows = list(reader)
    seen = set()
    for row in rows:
        if row["query_id"] not in query_ids: raise ValueError("qrels contains unknown query")
        if (row["query_id"], row["document_id"]) in seen: raise ValueError("duplicate qrels judgment")
        grade = int(row["relevance_grade"])
        if grade not in range(4): raise ValueError("qrels grade must be 0..3")
        seen.add((row["query_id"], row["document_id"]))
    if {row["query_id"] for row in rows} != query_ids: raise ValueError("qrels must cover every approved query")
    return rows


def assign(rows, seed):
    groups = defaultdict(list)
    for row in rows:
        key = (row["category"], row["project"], row["difficulty"], row["freshness_sensitive"])
        groups[key].append(row["query_id"])
    result = {}
    for key in sorted(groups):
        values = groups[key][:]
        random.Random(f"{seed}|{key}").shuffle(values)
        for index, query_id in enumerate(values):
            bucket = index % 10
            result[query_id] = "development" if bucket < 6 else ("validation" if bucket < 8 else "test")
    return result


def freeze(args):
    if args.output_dir.exists() and any(args.output_dir.iterdir()): raise ValueError("output directory is non-empty; frozen split cannot be silently regenerated")
    rows = load_queries(args.queries); query_ids = {row["query_id"] for row in rows}; qrels = load_qrels(args.qrels, query_ids)
    assignment = assign(rows, args.seed); args.output_dir.mkdir(parents=True, exist_ok=True)
    for split in SPLITS:
        selected = [row for row in rows if assignment[row["query_id"]] == split]
        fields = list(rows[0].keys())
        with (args.output_dir / f"queries-{split}.psv").open("w", newline="", encoding="utf-8") as handle:
            writer = csv.DictWriter(handle, fieldnames=fields, delimiter="|", lineterminator="\n"); writer.writeheader(); writer.writerows(selected)
    manifest = {"schemaVersion": "research-split-manifest-v1", "corpusVersion": args.corpus_version, "qrelsVersion": args.qrels_version,
                "sourceQueriesSha256": sha256(args.queries), "qrelsSha256": sha256(args.qrels), "seed": args.seed,
                "algorithm": "stratified deterministic round-robin 60/20/20", "codeRevision": args.code_revision,
                "fractions": {"development": 0.6, "validation": 0.2, "test": 0.2}, "counts": {split: sum(value == split for value in assignment.values()) for split in SPLITS},
                "assignments": dict(sorted(assignment.items())), "frozen": True, "heldoutFlagRequired": "--final-heldout-evaluation"}
    (args.output_dir / "split-manifest.json").write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return manifest


def main():
    parser = argparse.ArgumentParser(description=__doc__); parser.add_argument("--queries", type=Path, required=True); parser.add_argument("--qrels", type=Path, required=True); parser.add_argument("--output-dir", type=Path, required=True); parser.add_argument("--corpus-version", required=True); parser.add_argument("--qrels-version", required=True); parser.add_argument("--seed", type=int, required=True); parser.add_argument("--code-revision", required=True); args = parser.parse_args()
    try: manifest = freeze(args); print(json.dumps({"status": "frozen", "counts": manifest["counts"]}, sort_keys=True)); return 0
    except (OSError, ValueError, csv.Error) as exc: print(f"ERROR: {exc}", file=sys.stderr); return 1


if __name__ == "__main__": raise SystemExit(main())
