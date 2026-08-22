#!/usr/bin/env python3
"""Regression test for annotation validation."""

import tempfile
from pathlib import Path

from validate_annotations import validate


def main():
    with tempfile.TemporaryDirectory() as directory:
        root = Path(directory); corpus = root / "corpus"; corpus.mkdir()
        (corpus / "a.md").write_text("answer", encoding="utf-8")
        queries = root / "queries.psv"; queries.write_text("q1|example|a.md:3\n", encoding="utf-8")
        annotations = root / "annotations.psv"; annotations.write_text(
            "q1|a.md|annotator-a|3|direct evidence\nq1|a.md|annotator-b|2|supporting evidence\n", encoding="utf-8")
        result = validate(annotations, queries, corpus)
        assert result["status"] == "valid" and result["annotators"] == 2
        annotations.write_text("q1|a.md|annotator-a|4|invalid\n", encoding="utf-8")
        try:
            validate(annotations, queries, corpus)
        except ValueError:
            print("test_validate_annotations: PASS")
            return
        raise AssertionError("invalid grade was accepted")


if __name__ == "__main__":
    main()
