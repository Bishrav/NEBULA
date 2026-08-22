#!/usr/bin/env python3
"""Gate an annotation release before it is used for research claims."""

import argparse
import json
import sys
from pathlib import Path

from validate_annotations import validate


def validate_release(annotations, queries, corpus_dir, agreement_path, adjudication_path, minimum_kappa=None):
    validation = validate(annotations, queries, corpus_dir)
    agreement = json.loads(Path(agreement_path).read_text(encoding="utf-8"))
    packet = json.loads(Path(adjudication_path).read_text(encoding="utf-8"))
    if len(agreement.get("annotators", [])) < 2:
        raise ValueError("release requires at least two annotators")
    pairs = agreement.get("pairs", [])
    if not pairs:
        raise ValueError("agreement report contains no annotator pairs")
    if minimum_kappa is not None:
        low = [pair for pair in pairs if pair.get("cohensKappa", -1.0) < minimum_kappa]
        if low:
            raise ValueError(f"agreement below minimum kappa {minimum_kappa}: {len(low)} pair(s)")
    unresolved = []
    for item in packet.get("items", []):
        if item.get("adjudicatedGrade") is None or not item.get("adjudicatorId") or not item.get("decisionNote"):
            unresolved.append(f"{item.get('queryId')}:{item.get('sourcePath')}")
    if unresolved:
        raise ValueError("unresolved adjudication items: " + ", ".join(unresolved))
    return {"status": "release-ready", "annotationRows": validation["rows"], "annotators": len(agreement["annotators"]),
            "agreementPairs": len(pairs), "adjudicatedItems": len(packet.get("items", []))}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("annotations", type=Path)
    parser.add_argument("--queries", type=Path, required=True)
    parser.add_argument("--corpus-dir", type=Path, required=True)
    parser.add_argument("--agreement", type=Path, required=True)
    parser.add_argument("--adjudication", type=Path, required=True)
    parser.add_argument("--minimum-kappa", type=float)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    try:
        result = validate_release(args.annotations, args.queries, args.corpus_dir, args.agreement, args.adjudication, args.minimum_kappa)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        print(f"RELEASE READY: {args.output}")
        return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
