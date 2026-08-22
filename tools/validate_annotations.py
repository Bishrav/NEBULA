#!/usr/bin/env python3
"""Validate independent relevance annotations against a frozen query/corpus set."""

import argparse
import json
import sys
from pathlib import Path


def query_ids(path):
    values = set()
    for line in path.read_text(encoding="utf-8").splitlines():
        row = line.strip()
        if not row or row.startswith("#") or row.startswith("query_id|"):
            continue
        parts = row.split("|", 2)
        if len(parts) != 3:
            raise ValueError(f"invalid query row: {line}")
        values.add(parts[0].strip())
    return values


def validate(annotation_path, query_path, corpus_dir):
    allowed_queries = query_ids(query_path)
    allowed_sources = {p.relative_to(corpus_dir).as_posix() for p in corpus_dir.rglob("*") if p.is_file()}
    seen = set()
    annotators = set()
    rows = 0
    grades = []
    for line_number, line in enumerate(annotation_path.read_text(encoding="utf-8").splitlines(), 1):
        row = line.strip()
        if not row or row.startswith("#") or row.startswith("query_id|"):
            continue
        parts = row.split("|", 4)
        if len(parts) != 5:
            raise ValueError(f"line {line_number}: expected query_id|source_path|annotator_id|grade|evidence_note")
        query_id, source, annotator, grade_text, note = (part.strip() for part in parts)
        if query_id not in allowed_queries:
            raise ValueError(f"line {line_number}: unknown query: {query_id}")
        if source not in allowed_sources:
            raise ValueError(f"line {line_number}: unknown source: {source}")
        if not annotator or not note:
            raise ValueError(f"line {line_number}: annotator and evidence note are required")
        try:
            grade = int(grade_text)
        except ValueError as exc:
            raise ValueError(f"line {line_number}: grade must be an integer") from exc
        if grade < 0 or grade > 3:
            raise ValueError(f"line {line_number}: grade must be between 0 and 3")
        key = (query_id, source, annotator)
        if key in seen:
            raise ValueError(f"line {line_number}: duplicate annotation: {key}")
        seen.add(key); annotators.add(annotator); grades.append(grade); rows += 1
    if rows == 0:
        raise ValueError("annotation file contains no records")
    return {"status": "valid", "rows": rows, "annotators": len(annotators), "positiveGrades": sum(g > 0 for g in grades)}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("annotations", type=Path)
    parser.add_argument("--queries", type=Path, required=True)
    parser.add_argument("--corpus-dir", type=Path, required=True)
    parser.add_argument("--json", type=Path)
    args = parser.parse_args()
    try:
        result = validate(args.annotations, args.queries, args.corpus_dir)
        payload = json.dumps(result, indent=2, sort_keys=True) + "\n"
        if args.json:
            args.json.parent.mkdir(parents=True, exist_ok=True); args.json.write_text(payload, encoding="utf-8")
        print(payload, end="")
        return 0
    except (OSError, ValueError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
