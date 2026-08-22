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
    linear = weighted_kappa(left_values, right_values, "linear")
    quadratic = weighted_kappa(left_values, right_values, "quadratic")
    return {"annotatorA": left, "annotatorB": right, "overlap": len(common), "agreement": observed,
            "cohensKappa": kappa, "weightedKappaLinear": linear, "weightedKappaQuadratic": quadratic,
            "disagreements": len(common) - round(observed * len(common))}


def weighted_kappa(left_values, right_values, kind):
    """Weighted Cohen's kappa for ordinal grades 0..3.

    Agreement weights are one minus normalized grade distance. Linear and
    quadratic distance make the penalty for near versus far disagreements
    explicit instead of treating all disagreements as nominal categories.
    """
    categories = range(4)

    def weight(left, right):
        distance = abs(left - right) / 3.0
        return 1.0 - (distance if kind == "linear" else distance * distance)

    observed = sum(weight(left, right) for left, right in zip(left_values, right_values)) / len(left_values)
    left_counts = [left_values.count(category) for category in categories]
    right_counts = [right_values.count(category) for category in categories]
    expected = sum(left_counts[left] * right_counts[right] * weight(left, right)
                   for left in categories for right in categories) / (len(left_values) * len(right_values))
    if expected == 1.0:
        return 1.0 if observed == 1.0 else 0.0
    return (observed - expected) / (1.0 - expected)


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
