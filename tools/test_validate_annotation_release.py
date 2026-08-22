#!/usr/bin/env python3
"""Regression test for the annotation release gate."""

import json
import tempfile
from pathlib import Path

from analyze_annotation_agreement import analyze
from generate_adjudication_packet import build_packet
from validate_annotation_release import validate_release


def main():
    with tempfile.TemporaryDirectory() as directory:
        root = Path(directory); corpus = root / "corpus"; corpus.mkdir()
        (corpus / "a.md").write_text("answer", encoding="utf-8")
        queries = root / "queries.psv"; queries.write_text("q1|example|a.md:3\n", encoding="utf-8")
        annotations = root / "annotations.psv"; annotations.write_text(
            "q1|a.md|a|3|direct\nq1|a.md|b|2|support\n", encoding="utf-8")
        agreement = root / "agreement.json"; agreement.write_text(json.dumps(analyze(annotations, queries, corpus)), encoding="utf-8")
        packet = build_packet(annotations, queries, corpus); packet["items"][0].update({"adjudicatedGrade": 3, "adjudicatorId": "lead", "decisionNote": "Direct evidence wins under the guide."})
        adjudication = root / "adjudication.json"; adjudication.write_text(json.dumps(packet), encoding="utf-8")
        result = validate_release(annotations, queries, corpus, agreement, adjudication)
        assert result["status"] == "release-ready"
        print("test_validate_annotation_release: PASS")


if __name__ == "__main__":
    main()
