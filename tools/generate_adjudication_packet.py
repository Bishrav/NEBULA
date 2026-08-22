#!/usr/bin/env python3
"""Create a human adjudication packet without resolving disagreements automatically."""

import argparse
import json
import sys
from collections import defaultdict
from pathlib import Path

from validate_annotations import validate


def load(path):
    groups = defaultdict(list)
    for line in path.read_text(encoding="utf-8").splitlines():
        row = line.strip()
        if not row or row.startswith("#") or row.startswith("query_id|"):
            continue
        query, source, annotator, grade, note = (part.strip() for part in row.split("|", 4))
        groups[(query, source)].append({"annotatorId": annotator, "grade": int(grade), "evidenceNote": note})
    return groups


def build_packet(annotation_path, query_path, corpus_dir):
    validation = validate(annotation_path, query_path, corpus_dir)
    groups = load(annotation_path)
    items = []
    for (query, source), labels in sorted(groups.items()):
        grades = sorted({label["grade"] for label in labels})
        if len(grades) > 1:
            items.append({"queryId": query, "sourcePath": source, "independentLabels": labels,
                          "adjudicatedGrade": None, "adjudicatorId": None, "decisionNote": None})
    return {"schemaVersion": "adjudication-packet-v1", "sourceValidation": validation,
            "disagreementCount": len(items), "items": items,
            "instruction": "A qualified adjudicator must complete blank fields; this packet does not infer a winning label."}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("annotations", type=Path)
    parser.add_argument("--queries", type=Path, required=True)
    parser.add_argument("--corpus-dir", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    try:
        packet = build_packet(args.annotations, args.queries, args.corpus_dir)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(packet, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        print(f"GENERATED: {args.output} ({packet['disagreementCount']} disagreements)")
        return 0
    except (OSError, ValueError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
