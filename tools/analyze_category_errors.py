#!/usr/bin/env python3
"""Summarize category-level retrieval performance and query failures."""

import argparse
import csv
import json
from collections import defaultdict
from pathlib import Path


def read_metadata(path: Path) -> dict[str, dict[str, str]]:
    metadata = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or line.startswith("query_id|"):
            continue
        query_id, category, freshness, evidence = line.split("|")
        metadata[query_id] = {
            "category": category,
            "freshnessExpectation": freshness,
            "evidenceSufficient": evidence.lower() == "true",
        }
    return metadata


def load_benchmark(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def mean(values: list[float]) -> float:
    return sum(values) / len(values) if values else 0.0


def analyze(benchmark: dict, metadata: dict[str, dict[str, str]]) -> dict:
    variants = {row["name"]: {item["queryId"]: item for item in row["perQuery"]} for row in benchmark["variants"]}
    baseline = variants["bm25"]
    categories = sorted({metadata[qid]["category"] for qid in baseline if qid in metadata})
    category_rows = []
    failures = []
    for variant, query_rows in variants.items():
        for category in categories:
            qids = sorted(qid for qid in query_rows if qid in metadata and metadata[qid]["category"] == category)
            rows = [query_rows[qid] for qid in qids]
            baseline_rows = [baseline[qid] for qid in qids]
            deltas = [row["ndcgAtK"] - base["ndcgAtK"] for row, base in zip(rows, baseline_rows)]
            category_rows.append({
                "variant": variant,
                "category": category,
                "queryCount": len(rows),
                "meanNdcgAtK": round(mean([row["ndcgAtK"] for row in rows]), 6),
                "meanMrr": round(mean([row["mrr"] for row in rows]), 6),
                "meanDeltaNdcgVsBm25": round(mean(deltas), 6),
                "winsVsBm25": sum(delta > 1e-9 for delta in deltas),
                "lossesVsBm25": sum(delta < -1e-9 for delta in deltas),
                "zeroNdcgQueries": sum(row["ndcgAtK"] <= 1e-9 for row in rows),
            })
        if variant == "bm25":
            continue
        for qid, row in query_rows.items():
            if qid not in baseline or qid not in metadata:
                continue
            delta = row["ndcgAtK"] - baseline[qid]["ndcgAtK"]
            if row["recallAtK"] < 1.0 or delta < -1e-9:
                failures.append({
                    "variant": variant,
                    "queryId": qid,
                    "category": metadata[qid]["category"],
                    "freshnessExpectation": metadata[qid]["freshnessExpectation"],
                    "recallAtK": row["recallAtK"],
                    "ndcgAtK": row["ndcgAtK"],
                    "bm25NdcgAtK": baseline[qid]["ndcgAtK"],
                    "deltaNdcgVsBm25": round(delta, 6),
                    "failureReason": "missed_relevance" if row["recallAtK"] < 1.0 else "ranking_regression",
                })
    return {
        "schemaVersion": "category-error-analysis-v1",
        "evidenceStatus": "REGRESSION_FIXTURE_ONLY",
        "benchmarkSchemaVersion": benchmark.get("schemaVersion"),
        "queryCount": benchmark["queryCount"],
        "categoryCount": len(categories),
        "categories": categories,
        "categorySummaries": category_rows,
        "failures": sorted(failures, key=lambda row: (row["variant"], row["category"], row["queryId"])),
    }


def write_outputs(result: dict, output_json: Path, output_csv: Path, output_md: Path) -> None:
    output_json.parent.mkdir(parents=True, exist_ok=True)
    output_json.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    with output_csv.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=[
            "variant", "category", "queryCount", "meanNdcgAtK",
            "meanMrr", "meanDeltaNdcgVsBm25", "winsVsBm25",
            "lossesVsBm25", "zeroNdcgQueries",
        ])
        writer.writeheader()
        writer.writerows(result["categorySummaries"])
    lines = [
        "# Category and failure analysis",
        "",
        "> Generated from the synthetic regression fixture. This is descriptive engineering evidence, not a publication claim.",
        "",
        f"- Queries: {result['queryCount']}",
        f"- Categories: {result['categoryCount']}",
        f"- Recorded variant/query failures: {len(result['failures'])}",
        "",
        "## Category summaries",
        "",
        "| Variant | Category | Queries | Mean NDCG | Mean MRR | Delta vs BM25 | Wins | Losses | Zero-NDCG |",
        "| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |",
    ]
    for row in result["categorySummaries"]:
        lines.append(
            f"| {row['variant']} | {row['category']} | {row['queryCount']} | "
            f"{row['meanNdcgAtK']:.4f} | {row['meanMrr']:.4f} | "
            f"{row['meanDeltaNdcgVsBm25']:.4f} | {row['winsVsBm25']} | "
            f"{row['lossesVsBm25']} | {row['zeroNdcgQueries']} |"
        )
    lines.extend(["", "## Failures and regressions", "", "| Variant | Query | Category | Reason | NDCG | BM25 NDCG | Delta |", "| --- | --- | --- | --- | ---: | ---: | ---: |"])
    for row in result["failures"]:
        lines.append(
            f"| {row['variant']} | {row['queryId']} | {row['category']} | "
            f"{row['failureReason']} | {row['ndcgAtK']:.4f} | "
            f"{row['bm25NdcgAtK']:.4f} | {row['deltaNdcgVsBm25']:.4f} |"
        )
    output_md.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--benchmark", type=Path, required=True)
    parser.add_argument("--metadata", type=Path, required=True)
    parser.add_argument("--output-json", type=Path, required=True)
    parser.add_argument("--output-csv", type=Path, required=True)
    parser.add_argument("--output-md", type=Path, required=True)
    args = parser.parse_args()
    write_outputs(
        analyze(load_benchmark(args.benchmark), read_metadata(args.metadata)),
        args.output_json,
        args.output_csv,
        args.output_md,
    )
    print(f"GENERATED: {args.output_json}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
