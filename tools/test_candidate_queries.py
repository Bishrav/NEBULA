#!/usr/bin/env python3
import csv
import tempfile
import unittest
from pathlib import Path

from validate_candidate_queries import validate, FIELDS


class CandidateQueryTest(unittest.TestCase):
    def write(self, rows):
        directory = tempfile.TemporaryDirectory(); self.addCleanup(directory.cleanup)
        path = Path(directory.name) / "queries.psv"
        with path.open("w", newline="", encoding="utf-8") as handle:
            writer = csv.DictWriter(handle, fieldnames=FIELDS, delimiter="|"); writer.writeheader(); writer.writerows(rows)
        return path

    def row(self, **changes):
        row = dict(zip(FIELDS, ["q1", "How should an engineer configure secure networking for a Kubernetes API server?", "networking", "Kubernetes", "intermediate", "false", "true", "true", "true", "manual candidate", "doc-a", "a" * 64, "NEEDS_HUMAN_REVIEW"]))
        row.update(changes); return row

    def test_valid_candidate(self):
        result = validate(self.write([self.row()]))
        self.assertEqual(result["candidates"], 1); self.assertFalse(result["qrels_created"])

    def test_duplicate_and_title_like_rejected(self):
        with self.assertRaises(ValueError): validate(self.write([self.row(), self.row(query_id="q2")]))
        with self.assertRaises(ValueError): validate(self.write([self.row(query_text="What is Kubernetes?")]))


if __name__ == "__main__": unittest.main()
