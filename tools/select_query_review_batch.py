#!/usr/bin/env python3
"""Select a deterministic, balanced human-review batch without creating qrels."""

import argparse
import csv
import hashlib
import json
from collections import Counter
from pathlib import Path


def stable_key(row):
    return hashlib.sha256(row["query_id"].encode("utf-8")).hexdigest()


def select(rows, per_stratum=2, extra_per_project=4):
    groups = {}
    for row in rows:
        groups.setdefault((row["project"], row["category"]), []).append(row)
    selected = []
    for key in sorted(groups):
        candidates = sorted(groups[key], key=stable_key)
        selected.extend(candidates[:per_stratum])

    selected_ids = {row["query_id"] for row in selected}
    by_project = {}
    for row in rows:
        if row["query_id"] not in selected_ids:
            by_project.setdefault(row["project"], []).append(row)
    for project in sorted(by_project):
        candidates = sorted(
            by_project[project],
            key=lambda row: (row["difficulty"] != "advanced", stable_key(row)),
        )
        selected.extend(candidates[:extra_per_project])

    selected = sorted(selected, key=lambda row: (row["project"], row["category"], stable_key(row)))
    projects = {row["project"] for row in rows}
    categories = {row["category"] for row in rows}
    expected = per_stratum * len(projects) * len(categories) + extra_per_project * len(projects)
    if len(selected) != expected:
        raise ValueError("selection size does not match the configured strata")
    if any(row["review_status"] != "NEEDS_HUMAN_REVIEW" for row in selected):
        raise ValueError("selection included a non-review candidate")
    return selected


def write_csv(path, rows):
    fields = [
        "query_id", "query_text", "category", "project", "difficulty",
        "freshness_sensitive", "authority_sensitive", "exact_identifier_dependency",
        "evidence_required", "grounding_document_id", "review_status",
        "reviewer_decision", "reviewer_notes",
    ]
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        for row in rows:
            writer.writerow({field: row.get(field, "") for field in fields})


def write_markdown(path, rows):
    projects = Counter(row["project"] for row in rows)
    categories = Counter(row["category"] for row in rows)
    difficulties = Counter(row["difficulty"] for row in rows)
    lines = [
        "# Human query review batch 01",
        "",
        "This batch is a deterministic review sample. It contains no relevance judgements and no approved queries.",
        "",
        "Review each query for realism, clarity, category, difficulty, grounding, freshness sensitivity, authority sensitivity, and evidence requirement.",
        "Use APPROVE, REWRITE, or REJECT in the reviewer_decision column. Record the reason in reviewer_notes.",
        "",
        "## Distribution",
        "",
        "| Dimension | Values |",
        "| --- | --- |",
        "| Total | {} |".format(len(rows)),
        "| Projects | {} |".format(", ".join("{}: {}".format(k, v) for k, v in sorted(projects.items()))),
        "| Categories | {} |".format(", ".join("{}: {}".format(k, v) for k, v in sorted(categories.items()))),
        "| Difficulty | {} |".format(", ".join("{}: {}".format(k, v) for k, v in sorted(difficulties.items()))),
        "",
        "## Review rule",
        "",
        "Do not assign document relevance labels in this batch. Approval means only that the information need is suitable for a later retrieval-judgement study.",
        "",
    ]
    path.write_text("\n".join(lines), encoding="utf-8")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--queries", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    args = parser.parse_args()
    with args.queries.open(encoding="utf-8", newline="") as handle:
        rows = list(csv.DictReader(handle, delimiter="|"))
    selected = select(rows)
    args.output_dir.mkdir(parents=True, exist_ok=True)
    write_csv(args.output_dir / "review-batch-01.csv", selected)
    (args.output_dir / "review-batch-01.psv").write_text(
        args.queries.read_text(encoding="utf-8").splitlines()[0] + "\n" +
        "\n".join("|".join(row[field] for field in rows[0]) for row in selected) + "\n",
        encoding="utf-8",
    )
    write_markdown(args.output_dir / "review-batch-01.md", selected)
    metadata = {
        "schema_version": "query-review-batch-v1",
        "batch_id": "review-batch-01",
        "candidate_count": len(rows),
        "selected_count": len(selected),
        "selection_rule": "two stable-hash selections per project/category stratum plus four advanced-first diversity selections per project",
        "approved_queries": 0,
        "qrels_created": False,
    }
    (args.output_dir / "review-batch-01.json").write_text(json.dumps(metadata, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(json.dumps(metadata, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
