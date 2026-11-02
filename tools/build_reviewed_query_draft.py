#!/usr/bin/env python3
"""Build a draft query set from human query-review decisions."""

import argparse
import csv
import json
import re
from collections import Counter
from pathlib import Path


CATEGORIES = {
    "api usage", "architecture", "compatibility", "configuration",
    "database administration", "debugging", "deployment", "failure recovery",
    "migration", "networking", "observability", "performance", "security",
}

CANONICAL_CATEGORIES = {category.lower(): ("API usage" if category == "api usage" else category) for category in CATEGORIES}


def proposed_text(note, original):
    match = re.search(r"rewrite as:\s*(.+?\?)", note, re.I)
    if match:
        return match.group(1).strip()
    return original


def proposed_category(note, original):
    lowered = note.lower()
    matches = []
    for category in sorted(CATEGORIES, key=len, reverse=True):
        if category in lowered:
            matches.append(category)
    if "recategorize" not in lowered or not matches:
        return original, "UNCHANGED"
    unique = []
    for category in matches:
        if category not in unique:
            unique.append(category)
    candidates = [category for category in unique if category != original.lower()]
    if len(candidates) == 1:
        return CANONICAL_CATEGORIES[candidates[0]], "REVIEWED_OVERRIDE"
    return original, "CATEGORY_REVIEW_REQUIRED"


def reviewed_categories(review, original):
    primary = (review.get("reviewed_primary_category") or "").strip()
    secondary = (review.get("secondary_categories") or "").strip()
    if primary:
        key = primary.lower()
        if key not in CANONICAL_CATEGORIES:
            raise ValueError(f"invalid reviewed primary category for {review['query_id']}: {primary}")
        return CANONICAL_CATEGORIES[key], secondary, "REVIEWED_OVERRIDE"
    category, status = proposed_category(review["reviewer_notes"], original)
    return category, secondary, status


def build(original_path, completed_path):
    with original_path.open(encoding="utf-8", newline="") as handle:
        original = {row["query_id"]: row for row in csv.DictReader(handle, delimiter="|")}
    with completed_path.open(encoding="utf-8-sig", newline="") as handle:
        completed = list(csv.DictReader(handle))
    if set(original) != {row["query_id"] for row in completed}:
        raise ValueError("completed review IDs do not exactly match the original batch")

    draft = []
    decisions = Counter()
    for review in completed:
        decision = review["reviewer_decision"].strip().upper()
        decisions[decision] += 1
        if decision == "REJECT":
            continue
        if decision not in {"APPROVE", "REWRITE"}:
            raise ValueError(f"invalid decision for {review['query_id']}: {decision}")
        source = original[review["query_id"]]
        text = source["query_text"] if decision == "APPROVE" else proposed_text(review["reviewer_notes"], source["query_text"])
        if decision == "REWRITE" and text == source["query_text"]:
            raise ValueError(f"rewrite has no proposed query text: {review['query_id']}")
        category, secondary, category_status = reviewed_categories(review, source["category"])
        draft.append({
            "query_id": source["query_id"],
            "query_text": text,
            "original_query_text": source["query_text"],
            "category": category,
            "secondary_categories": secondary,
            "category_status": category_status,
            "project": source["project"],
            "difficulty": source["difficulty"],
            "grounding_document_id": source["grounding_document_id"],
            "reviewer_decision": decision,
            "reviewer_notes": review["reviewer_notes"],
        })
    return {
        "schema_version": "reviewed-query-draft-v2",
        "source_batch": "review-batch-01",
        "draft_count": len(draft),
        "approved_count": decisions["APPROVE"],
        "rewritten_count": decisions["REWRITE"],
        "rejected_count": decisions["REJECT"],
        "category_review_required": sum(item["category_status"] == "CATEGORY_REVIEW_REQUIRED" for item in draft),
        "qrels_created": False,
        "benchmark_ready": False,
        "queries": draft,
    }


def write_csv(path, report):
    fields = list(report["queries"][0]) if report["queries"] else []
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        writer.writerows(report["queries"])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--original", type=Path, required=True)
    parser.add_argument("--completed", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    args = parser.parse_args()
    report = build(args.original, args.completed)
    args.output_dir.mkdir(parents=True, exist_ok=True)
    (args.output_dir / "reviewed-query-draft.json").write_text(json.dumps(report, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    write_csv(args.output_dir / "reviewed-query-draft.csv", report)
    print(json.dumps({key: value for key, value in report.items() if key != "queries"}, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
