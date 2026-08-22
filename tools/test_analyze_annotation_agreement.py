#!/usr/bin/env python3
"""Regression test for agreement metrics on synthetic annotations."""

import tempfile
from pathlib import Path

from analyze_annotation_agreement import analyze


def main():
    with tempfile.TemporaryDirectory() as directory:
        root = Path(directory); corpus = root / "corpus"; corpus.mkdir()
        (corpus / "a.md").write_text("answer", encoding="utf-8")
        queries = root / "queries.psv"; queries.write_text("q1|example|a.md:3\nq2|other|a.md:1\n", encoding="utf-8")
        annotations = root / "annotations.psv"; annotations.write_text(
            "q1|a.md|a|3|direct\nq2|a.md|a|1|context\nq1|a.md|b|3|direct\nq2|a.md|b|0|not relevant\n", encoding="utf-8")
        result = analyze(annotations, queries, corpus)
        pair = result["pairs"][0]
        assert pair["overlap"] == 2 and pair["agreement"] == 0.5
        print("test_analyze_annotation_agreement: PASS")


if __name__ == "__main__":
    main()
