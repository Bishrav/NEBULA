#!/usr/bin/env python3
"""Prepare a consensus release record from validated canonical annotations."""

import argparse
import csv
import json
import sys
from pathlib import Path

FIELDS = ["annotator_code", "query_id", "document_id", "grade", "uncertainty", "evidence_note", "annotated_at"]


def load(path):
    with path.open(encoding="utf-8", newline="") as handle:
        reader = csv.DictReader(handle, delimiter="|")
        if reader.fieldnames != FIELDS:
            raise ValueError(f"{path}: canonical annotation schema mismatch")
        rows = list(reader)
    if not rows:
        raise ValueError(f"{path}: no rows")
    code = rows[0]["annotator_code"].strip()
    records = {}
    for row in rows:
        if row["annotator_code"].strip() != code:
            raise ValueError(f"{path}: inconsistent annotator code")
        key = (row["query_id"].strip(), row["document_id"].strip())
        if key in records:
            raise ValueError(f"{path}: duplicate pair {key}")
        grade = int(row["grade"])
        if grade not in range(4):
            raise ValueError(f"{path}: grade must be 0..3")
        records[key] = grade
    return code, records


def build(annotation_paths, agreement_path):
    loaded = [load(path) for path in annotation_paths]
    codes = [code for code, _ in loaded]
    if len(set(codes)) != len(codes) or len(codes) < 2:
        raise ValueError("release requires at least two unique annotators")
    agreement = json.loads(agreement_path.read_text(encoding="utf-8"))
    if agreement.get("disagreementCount") != 0:
        raise ValueError("consensus release requires zero disagreements")
    common = set(loaded[0][1])
    for _, records in loaded[1:]:
        common &= set(records)
    if not common:
        raise ValueError("annotators have no common pairs")
    if any(set(records) != common for _, records in loaded):
        raise ValueError("annotators do not cover the same pairs")
    items = []
    for query_id, document_id in sorted(common):
        grades = [records[(query_id, document_id)] for _, records in loaded]
        if len(set(grades)) != 1:
            raise ValueError(f"grade disagreement for {query_id}/{document_id}")
        grade = grades[0]
        items.append({
            "queryId": query_id,
            "sourcePath": document_id,
            "documentId": document_id,
            "independentLabels": [{"annotatorId": code, "grade": grade} for code, _ in loaded],
            "adjudicatedGrade": grade,
            "adjudicatorId": "consensus-no-disagreement",
            "decisionNote": "Both independent annotators assigned the same grade; no adjudication disagreement required.",
        })
    return {
        "schemaVersion": "consensus-adjudication-packet-v1",
        "sourceAgreement": agreement,
        "disagreementCount": 0,
        "items": items,
        "instruction": "Consensus record created because all independent annotators agreed; no labels were changed.",
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--annotations", nargs="+", type=Path, required=True)
    parser.add_argument("--agreement", type=Path, required=True)
    parser.add_argument("--adjudication", type=Path, required=True)
    parser.add_argument("--release", type=Path, required=True)
    args = parser.parse_args()
    try:
        packet = build(args.annotations, args.agreement)
        args.adjudication.parent.mkdir(parents=True, exist_ok=True)
        args.adjudication.write_text(json.dumps(packet, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        release = {
            "status": "release-ready",
            "annotationRows": len(packet["items"]),
            "annotators": len(packet["items"][0]["independentLabels"]) if packet["items"] else 0,
            "agreementPairs": len(packet["sourceAgreement"].get("pairs", [])),
            "adjudicatedItems": len(packet["items"]),
            "humanGenerated": True,
            "consensusNoDisagreement": True,
        }
        args.release.write_text(json.dumps(release, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        print(json.dumps(release, sort_keys=True))
        return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
