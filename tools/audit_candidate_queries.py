#!/usr/bin/env python3
"""Audit candidate-query quality before human approval and relevance annotation."""

import argparse
import csv
import json
import re
from collections import Counter
from pathlib import Path


def flags(row):
    query = re.sub(r"\s+", " ", row["query_text"]).strip()
    lowered = query.lower()
    result = []
    if len(query.split()) < 8:
        result.append("short")
    if re.search(r"^(what is|documentation for|about)\s+[^?]+\??$", query, re.I):
        result.append("title_like")
    if lowered.startswith("how should an engineer use the api or interface for "):
        result.append("mechanical_api_template")
    if lowered.startswith("how does ") and " fit into the architecture of " in lowered:
        result.append("mechanical_architecture_template")
    if "human review required" in row.get("source_generation_method", "").lower():
        result.append("generated_not_human_reviewed")
    if not row.get("grounding_document_id"):
        result.append("missing_grounding_document")
    return result


def audit(path):
    with path.open(encoding="utf-8", newline="") as handle:
        rows = list(csv.DictReader(handle, delimiter="|"))
    audited = []
    counts = Counter()
    for row in rows:
        row_flags = flags(row)
        counts.update(row_flags)
        audited.append({
            "query_id": row["query_id"],
            "project": row["project"],
            "category": row["category"],
            "query_text": row["query_text"],
            "flags": row_flags,
            "automated_disposition": "REVIEW_REQUIRED" if row_flags else "READY_FOR_HUMAN_REVIEW",
        })
    return {
        "schema_version": "candidate-query-quality-audit-v1",
        "candidate_count": len(rows),
        "flagged_count": sum(bool(item["flags"]) for item in audited),
        "flag_counts": dict(sorted(counts.items())),
        "human_review_required": True,
        "automated_disposition_is_not_approval": True,
        "queries": audited,
    }


def write_csv(path, report):
    fields = ["query_id", "project", "category", "query_text", "flags", "automated_disposition"]
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        for row in report["queries"]:
            writer.writerow(dict(row, flags=";".join(row["flags"])))


def write_markdown(path, report):
    lines = [
        "# Candidate query quality audit",
        "",
        "This is an automated triage report. It does not approve queries or create relevance judgements.",
        "",
        "Candidates: **{}**".format(report["candidate_count"]),
        "Flagged for explicit human inspection: **{}**".format(report["flagged_count"]),
        "",
        "## Flag counts",
        "",
        "| Flag | Count |",
        "| --- | ---: |",
    ]
    lines.extend("| FLAG {} | {} |".format(name, count) for name, count in report["flag_counts"].items())
    lines.extend(["", "## Review rule", "", "A reviewer must decide whether each query expresses a realistic engineering information need, is grounded in the admitted corpus, has the right category and difficulty, and is suitable for relevance annotation. Automated flags are prompts for inspection, not automatic rejection.", ""])
    path.write_text("\n".join(lines), encoding="utf-8")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--queries", type=Path, required=True)
    parser.add_argument("--output-json", type=Path, required=True)
    parser.add_argument("--output-csv", type=Path, required=True)
    parser.add_argument("--output-md", type=Path, required=True)
    args = parser.parse_args()
    report = audit(args.queries)
    for path in (args.output_json, args.output_csv, args.output_md):
        path.parent.mkdir(parents=True, exist_ok=True)
    args.output_json.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    write_csv(args.output_csv, report)
    write_markdown(args.output_md, report)
    print(json.dumps({
        "candidates": report["candidate_count"],
        "flagged": report["flagged_count"],
        "flag_counts": report["flag_counts"],
    }, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
