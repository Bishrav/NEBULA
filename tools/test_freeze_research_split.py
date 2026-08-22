#!/usr/bin/env python3
import csv
import tempfile
import unittest
from pathlib import Path

from freeze_research_split import freeze


class SplitTest(unittest.TestCase):
    def test_requires_approval_and_freezes(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); queries = root / "queries.psv"; qrels = root / "qrels.psv"; output = root / "split"
            fields = ["query_id", "query_text", "category", "project", "difficulty", "freshness_sensitive", "authority_sensitive", "exact_identifier_dependency", "evidence_required", "source_generation_method", "grounding_document_id", "grounding_excerpt_checksum", "review_status"]
            with queries.open("w", newline="", encoding="utf-8") as handle:
                writer = csv.DictWriter(handle, fieldnames=fields, delimiter="|"); writer.writeheader()
                for i in range(10): writer.writerow(dict(zip(fields, [f"q{i}", f"How should an engineer configure system {i}?", "configuration", "Kubernetes", "intermediate", "false", "false", "false", "true", "human", "d", "a" * 64, "APPROVED"])))
            qrels.write_text("query_id|document_id|relevance_grade\n" + "\n".join(f"q{i}|d|2" for i in range(10)) + "\n", encoding="utf-8")
            args = type("Args", (), {"queries": queries, "qrels": qrels, "output_dir": output, "corpus_version": "research-corpus-v1", "qrels_version": "qrels-v1", "seed": 7, "code_revision": "test"})()
            manifest = freeze(args); self.assertEqual(sum(manifest["counts"].values()), 10); self.assertTrue(manifest["frozen"])
            with self.assertRaises(ValueError): freeze(args)


if __name__ == "__main__": unittest.main()
