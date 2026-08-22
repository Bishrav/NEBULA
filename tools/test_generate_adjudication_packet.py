#!/usr/bin/env python3
"""Regression test for non-destructive adjudication packet generation."""

import tempfile
from pathlib import Path

from generate_adjudication_packet import build_packet


def main():
    with tempfile.TemporaryDirectory() as directory:
        root = Path(directory); corpus = root / "corpus"; corpus.mkdir()
        (corpus / "a.md").write_text("answer", encoding="utf-8")
        queries = root / "queries.psv"; queries.write_text("q1|example|a.md:3\n", encoding="utf-8")
        annotations = root / "annotations.psv"; annotations.write_text(
            "q1|a.md|a|3|direct\nq1|a.md|b|1|context\n", encoding="utf-8")
        packet = build_packet(annotations, queries, corpus)
        assert packet["disagreementCount"] == 1
        assert packet["items"][0]["adjudicatedGrade"] is None
        print("test_generate_adjudication_packet: PASS")


if __name__ == "__main__":
    main()
