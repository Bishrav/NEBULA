#!/usr/bin/env python3
"""Create a human review sheet and distribution report for candidate queries."""

import argparse
import csv
from collections import Counter
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--queries", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    args = parser.parse_args()
    with args.queries.open(encoding="utf-8", newline="") as handle:
        rows = list(csv.DictReader(handle, delimiter="|"))
    args.output_dir.mkdir(parents=True, exist_ok=True)
    review_fields = ["query_id", "query_text", "category", "project", "difficulty", "grounding_document_id", "review_status", "reviewer_decision", "reviewer_notes"]
    with (args.output_dir / "query-review-sheet.csv").open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=review_fields); writer.writeheader()
        for row in rows: writer.writerow({field: row.get(field, "") for field in review_fields})
    categories, projects = Counter(row["category"] for row in rows), Counter(row["project"] for row in rows)
    lines = ["# Candidate query distribution report", "", "**Status:** `NEEDS_HUMAN_REVIEW` for every candidate. No qrels were created.", "", f"Total candidates: **{len(rows)}**", "", "## By category", "", "| Category | Count |", "| --- | ---: |"]
    lines += [f"| {key} | {value} |" for key, value in sorted(categories.items())]
    lines += ["", "## By project", "", "| Project | Count |", "| --- | ---: |"]
    lines += [f"| {key} | {value} |" for key, value in sorted(projects.items())]
    lines += ["", "## Review instructions", "", "Reject candidates that are vague, merely copy a document heading, depend on unavailable context, or do not express an information need. Approve only after a human confirms realism, category, difficulty, freshness/authority sensitivity, and evidence requirement. Do not assign relevance labels in this sheet.", ""]
    (args.output_dir / "query-distribution-report.md").write_text("\n".join(lines), encoding="utf-8")
    print(f"created review artifacts for {len(rows)} candidates")
    return 0


if __name__ == "__main__": raise SystemExit(main())
