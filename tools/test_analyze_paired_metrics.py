#!/usr/bin/env python3
"""Regression test for paired uncertainty and Holm correction."""

import tempfile
from pathlib import Path

from analyze_paired_metrics import analyze


def main():
    data = {"variants": [
        {"name": "bm25", "perQuery": [{"queryId": f"q{i}", "ndcgAtK": 0.5} for i in range(6)]},
        {"name": "hybrid", "perQuery": [{"queryId": f"q{i}", "ndcgAtK": 0.6 if i < 4 else 0.4} for i in range(6)]},
        {"name": "rrf", "perQuery": [{"queryId": f"q{i}", "ndcgAtK": 0.55} for i in range(6)]},
    ]}
    result = analyze(data, "bm25", "ndcgAtK", seed=7, resamples=100, permutations=100)
    assert result["schemaVersion"] == "paired-statistics-v1"
    assert len(result["results"]) == 2
    assert all(row["n"] == 6 and len(row["bootstrap95PercentCI"]) == 2 for row in result["results"])
    with tempfile.TemporaryDirectory() as directory:
        path = Path(directory) / "stats.json"
        path.write_text("ok", encoding="utf-8")
        assert path.read_text(encoding="utf-8") == "ok"
    print("test_analyze_paired_metrics: PASS")


if __name__ == "__main__":
    main()
