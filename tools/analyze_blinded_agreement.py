#!/usr/bin/env python3
"""Analyze blinded annotation exports and emit disagreements without adjudicating them."""

import argparse
import itertools
import json
import sys
from pathlib import Path

from analyze_annotation_agreement import weighted_kappa
from validate_blinded_annotations import validate


def load(path):
    records = {}
    for line in path.read_text(encoding="utf-8").splitlines()[1:]:
        if not line.strip(): continue
        code, query, document, grade, *_ = (item.strip() for item in line.split("|", 6))
        records[(code, query, document)] = int(grade)
    return records


def analyze(paths, packet_paths):
    records = {}
    annotators = []
    for path, packet in zip(paths, packet_paths):
        result = validate(path, packet)
        code = result["annotatorCode"]
        annotators.append(code); records.update(load(path))
    pairs, disagreements = [], []
    for left, right in itertools.combinations(sorted(annotators), 2):
        left_keys = {(query, document) for code, query, document in records if code == left}
        right_keys = {(query, document) for code, query, document in records if code == right}
        common = sorted(left_keys & right_keys)
        if not common: raise ValueError(f"annotators have no overlap: {left}, {right}")
        left_values = [records[(left, query, document)] for query, document in common]
        right_values = [records[(right, query, document)] for query, document in common]
        observed = sum(a == b for a, b in zip(left_values, right_values)) / len(common)
        left_counts = [left_values.count(g) for g in range(4)]; right_counts = [right_values.count(g) for g in range(4)]
        expected = sum(a * b for a, b in zip(left_counts, right_counts)) / (len(common) * len(common))
        kappa = 1.0 if expected == 1.0 and observed == 1.0 else ((observed - expected) / (1.0 - expected) if expected != 1.0 else 0.0)
        pairs.append({"annotatorA": left, "annotatorB": right, "overlap": len(common), "agreement": observed,
                      "cohensKappa": kappa, "weightedKappaLinear": weighted_kappa(left_values, right_values, "linear"),
                      "weightedKappaQuadratic": weighted_kappa(left_values, right_values, "quadratic")})
        for query, document in common:
            if records[(left, query, document)] != records[(right, query, document)]:
                disagreements.append({"queryId": query, "documentId": document, "labels": {left: records[(left, query, document)], right: records[(right, query, document)]}})
    return {"schemaVersion": "blinded-annotation-agreement-v1", "annotators": sorted(annotators), "pairs": pairs,
            "disagreementCount": len(disagreements), "disagreements": disagreements, "humanAdjudicationRequired": True}


def main():
    parser = argparse.ArgumentParser(description=__doc__); parser.add_argument("--annotations", nargs="+", type=Path, required=True); parser.add_argument("--packets", nargs="+", type=Path, required=True); parser.add_argument("--output", type=Path, required=True); args = parser.parse_args()
    try:
        if len(args.annotations) != len(args.packets): raise ValueError("annotations and packets must have equal length")
        result = analyze(args.annotations, args.packets); args.output.parent.mkdir(parents=True, exist_ok=True); args.output.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8"); print(json.dumps({"status": "created", "disagreements": result["disagreementCount"]}, sort_keys=True)); return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc: print(f"ERROR: {exc}", file=sys.stderr); return 1


if __name__ == "__main__": raise SystemExit(main())
