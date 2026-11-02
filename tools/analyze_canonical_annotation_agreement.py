#!/usr/bin/env python3
"""Analyze agreement between validated canonical annotation exports."""

import argparse
import csv
import itertools
import json
import sys
from pathlib import Path

from analyze_annotation_agreement import weighted_kappa


FIELDS = ["annotator_code", "query_id", "document_id", "grade", "uncertainty", "evidence_note", "annotated_at"]


def load(path):
    with path.open(encoding="utf-8", newline="") as handle:
        reader = csv.DictReader(handle, delimiter="|")
        if reader.fieldnames != FIELDS:
            raise ValueError(f"{path}: canonical annotation schema mismatch")
        rows = list(reader)
    records = {}
    for row in rows:
        key = (row["query_id"].strip(), row["document_id"].strip())
        if key in records:
            raise ValueError(f"{path}: duplicate annotation pair")
        grade = int(row["grade"])
        if grade not in range(4):
            raise ValueError(f"{path}: grade must be 0..3")
        records[key] = grade
    if not records:
        raise ValueError(f"{path}: no annotations")
    annotator = rows[0]["annotator_code"].strip()
    if not annotator or any(row["annotator_code"].strip() != annotator for row in rows):
        raise ValueError(f"{path}: annotator code is inconsistent")
    return annotator, records


def analyze(paths):
    loaded = [load(path) for path in paths]
    if len({code for code, _ in loaded}) != len(loaded):
        raise ValueError("annotator codes must be unique")
    pairs = []
    disagreements = []
    for (left, left_records), (right, right_records) in itertools.combinations(loaded, 2):
        common = sorted(set(left_records) & set(right_records))
        if not common:
            raise ValueError(f"annotators have no overlap: {left}, {right}")
        left_values = [left_records[key] for key in common]
        right_values = [right_records[key] for key in common]
        observed = sum(a == b for a, b in zip(left_values, right_values)) / len(common)
        left_counts = [left_values.count(g) for g in range(4)]
        right_counts = [right_values.count(g) for g in range(4)]
        expected = sum(a * b for a, b in zip(left_counts, right_counts)) / (len(common) * len(common))
        kappa = 1.0 if expected == 1.0 and observed == 1.0 else ((observed - expected) / (1.0 - expected) if expected != 1.0 else 0.0)
        pairs.append({
            "annotatorA": left, "annotatorB": right, "overlap": len(common),
            "agreement": observed, "cohensKappa": kappa,
            "weightedKappaLinear": weighted_kappa(left_values, right_values, "linear"),
            "weightedKappaQuadratic": weighted_kappa(left_values, right_values, "quadratic"),
        })
        for key in common:
            if left_records[key] != right_records[key]:
                disagreements.append({"queryId": key[0], "documentId": key[1], "labels": {left: left_records[key], right: right_records[key]}})
    return {
        "schemaVersion": "canonical-annotation-agreement-v1",
        "annotators": sorted(code for code, _ in loaded),
        "pairs": pairs,
        "disagreementCount": len(disagreements),
        "disagreements": disagreements,
        "humanAdjudicationRequired": True,
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--annotations", nargs="+", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    try:
        result = analyze(args.annotations)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        print(json.dumps({"status": "created", "disagreements": result["disagreementCount"]}, sort_keys=True))
        return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
