#!/usr/bin/env python3
import csv
import tempfile
import unittest
from pathlib import Path

from build_reviewed_query_draft import build


class ReviewedQueryDraftTest(unittest.TestCase):
    def test_builds_only_non_rejected_queries_and_extracts_rewrites(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            original = root / "original.psv"
            original.write_text(
                "query_id|query_text|category|project|difficulty|grounding_document_id\n"
                "q1|Original question for Kubernetes?|architecture|Kubernetes|intermediate|doc-1\n"
                "q2|Original question for PostgreSQL?|performance|PostgreSQL|advanced|doc-2\n"
                "q3|Rejected question?|security|Kubernetes|intermediate|doc-3\n",
                encoding="utf-8",
            )
            completed = root / "completed.csv"
            completed.write_text(
                "query_id,reviewer_decision,reviewer_notes\n"
                "q1,APPROVE,Clear and realistic.\n"
                "q2,REWRITE,Rewrite as: How can an engineer measure query latency in PostgreSQL?\n"
                "q3,REJECT,Too vague.\n",
                encoding="utf-8",
            )
            report = build(original, completed)
            self.assertEqual(report["draft_count"], 2)
            self.assertEqual(report["rejected_count"], 1)
            self.assertEqual(report["queries"][1]["query_text"], "How can an engineer measure query latency in PostgreSQL?")
            self.assertFalse(report["benchmark_ready"])

    def test_rewrite_without_proposed_text_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            original = root / "original.psv"
            original.write_text(
                "query_id|query_text|category|project|difficulty|grounding_document_id\nq1|Original?|security|Kubernetes|intermediate|doc\n",
                encoding="utf-8",
            )
            completed = root / "completed.csv"
            completed.write_text(
                "query_id,reviewer_decision,reviewer_notes\nq1,REWRITE,Needs improvement.\n",
                encoding="utf-8",
            )
            with self.assertRaises(ValueError):
                build(original, completed)


if __name__ == "__main__":
    unittest.main()