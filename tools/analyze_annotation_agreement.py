#!/usr/bin/env python3
"""Compute pairwise nominal agreement and Cohen's kappa for annotations."""

import argparse
import itertools
import json
import sys
from collections import defaultdict
from pathlib import Path

from validate_annotations import validate


def load_records(path):
    records = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        row = line.strip()
        if not row or row.startswith("#") or row.startswith("query_id|"):
            continue
        query, source, annotator, grade, _note = (part.strip() for part in row.split("|", 4))
        records[(annotator, query, source)] = int(grade)
    return records


def pair_result(left, right, records):
    left_keys = {(query, source) for annotator, query, source in records if annotator == left}
    right_keys = {(query, source) for annotator, query, source in records if annotator == right}
    common = sorted(left_keys & right_keys)
    if not common:
        raise ValueError(f"annotators have no overlapping records: {left}, {right}")
    left_values = [records[(left, query, source)] for query, source in common]
    right_values = [records[(right, query, source)] for query, source in common]
    observed = sum(a == b for a, b in zip(left_values, right_values)) / len(common)
    left_counts = [left_values.count(grade) for grade in range(4)]
    right_counts = [right_values.count(grade) for grade in range(4)]
    expected = sum(a * b for a, b in zip(left_counts, right_counts)) / (len(common) * len(common))
    kappa = 1.0 if expected == 1.0 and observed == 1.0 else ((observed - expected) / (1.0 - expected) if expected != 1.0 else 0.0)
    return {"annotatorA": left, "annotatorB": right, "overlap": len(common), "agreement": observed, "cohensKappa": kappa, "disagreements": len(common) - round(observed * len(common))}


def analyze(path, query_path, corpus_dir):
    validate(path, query_path, corpus_dir)
    records = load_records(path)
    annotators = sorted({annotator for annotator, _query, _source in records})
    if len(annotators) < 2:
        raise ValueError("at least two annotators are required for agreement analysis")
    pairs = [pair_result(left, right, records) for left, right in itertools.combinations(annotators, 2)]
    return {"schemaVersion": "annotation-agreement-v1", "annotators": annotators, "pairs": pairs}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("annotations", type=Path)
    parser.add_argument("--queries", type=Path, required=True)
    parser.add_argument("--corpus-dir", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    try:
        result = analyze(args.annotations, args.queries, args.corpus_dir)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        print(f"GENERATED: {args.output}")
        return 0
    except (OSError, ValueError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
