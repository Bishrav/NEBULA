#!/usr/bin/env python3
"""Compare two completed authority reviews without adjudicating them."""

import argparse
import csv
import json
from pathlib import Path

from validate_authority_review import validate


def load(path):
    lines = path.read_text(encoding="utf-8").splitlines()
    rows = csv.DictReader((line for line in lines if line and not line.startswith("#")), delimiter="|")
    return {row["document_id"]: float(row["proposed_authority"]) for row in rows}


def compare(manifest, review_a, review_b, threshold=0.05):
    validation_a = validate(manifest, review_a)
    validation_b = validate(manifest, review_b)
    scores_a = load(review_a)
    scores_b = load(review_b)
    if set(scores_a) != set(scores_b):
        raise ValueError("reviews cover different documents")
    differences = [abs(scores_a[key] - scores_b[key]) for key in sorted(scores_a)]
    disagreements = [
        {"documentId": key, "reviewA": scores_a[key], "reviewB": scores_b[key], "absoluteDifference": abs(scores_a[key] - scores_b[key])}
        for key in sorted(scores_a)
        if abs(scores_a[key] - scores_b[key]) > threshold
    ]
    return {
        "schemaVersion": "authority-review-agreement-v1",
        "reviewA": validation_a,
        "reviewB": validation_b,
        "documents": len(differences),
        "threshold": threshold,
        "meanAbsoluteDifference": sum(differences) / len(differences),
        "exactAgreement": sum(value == 0 for value in differences) / len(differences),
        "withinThresholdAgreement": sum(value <= threshold for value in differences) / len(differences),
        "disagreementCount": len(disagreements),
        "disagreements": disagreements,
        "adjudicationRequired": bool(disagreements),
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--review-a", type=Path, required=True)
    parser.add_argument("--review-b", type=Path, required=True)
    parser.add_argument("--threshold", type=float, default=0.05)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    try:
        if not 0 <= args.threshold <= 1:
            raise ValueError("threshold must be between 0 and 1")
        result = compare(args.manifest, args.review_a, args.review_b, args.threshold)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        print(json.dumps({key: result[key] for key in ("documents", "disagreementCount", "adjudicationRequired")}, sort_keys=True))
        return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}")
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
