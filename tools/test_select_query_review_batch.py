#!/usr/bin/env python3
import unittest
from pathlib import Path

from select_query_review_batch import select


FIELDS = [
    "query_id", "query_text", "category", "project", "difficulty",
    "freshness_sensitive", "authority_sensitive", "exact_identifier_dependency",
    "evidence_required", "source_generation_method", "grounding_document_id",
    "grounding_excerpt_checksum", "review_status",
]


class QueryReviewBatchTest(unittest.TestCase):
    def row(self, number, project, category, difficulty="intermediate"):
        return dict(zip(FIELDS, [
            "q{}".format(number), "How can an engineer solve problem {} in {}?".format(number, project),
            category, project, difficulty, "True", "True", "False", "True",
            "human-authored draft", "doc-{}".format(number), "hash-{}".format(number),
            "NEEDS_HUMAN_REVIEW",
        ]))

    def test_selection_is_balanced_and_deterministic(self):
        rows = []
        number = 0
        for project in ("Kubernetes", "PostgreSQL"):
            for category in ("architecture", "security"):
                for _ in range(3):
                    number += 1
                    rows.append(self.row(number, project, category, "advanced" if number % 2 else "intermediate"))
        first = select(rows, per_stratum=2, extra_per_project=1)
        second = select(rows, per_stratum=2, extra_per_project=1)
        self.assertEqual([row["query_id"] for row in first], [row["query_id"] for row in second])
        self.assertEqual(len(first), 10)
        self.assertEqual({row["project"] for row in first}, {"Kubernetes", "PostgreSQL"})
        self.assertEqual(len({(row["project"], row["category"]) for row in first}), 4)
        self.assertTrue(all(row["review_status"] == "NEEDS_HUMAN_REVIEW" for row in first))


if __name__ == "__main__":
    unittest.main()
