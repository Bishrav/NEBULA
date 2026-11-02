import json
import tempfile
import unittest
from pathlib import Path

from analyze_category_errors import analyze, read_metadata, write_outputs


class CategoryErrorAnalysisTest(unittest.TestCase):
    def test_category_summary_and_failure_are_reproducible(self):
        benchmark = {
            "schemaVersion": "benchmark-v1",
            "queryCount": 2,
            "variants": [
                {
                    "name": "bm25",
                    "perQuery": [
                        {"queryId": "q1", "recallAtK": 1.0, "mrr": 1.0, "ndcgAtK": 1.0},
                        {"queryId": "q2", "recallAtK": 0.0, "mrr": 0.0, "ndcgAtK": 0.0},
                    ],
                },
                {
                    "name": "semantic",
                    "perQuery": [
                        {"queryId": "q1", "recallAtK": 1.0, "mrr": 1.0, "ndcgAtK": 0.5},
                        {"queryId": "q2", "recallAtK": 1.0, "mrr": 0.5, "ndcgAtK": 0.8},
                    ],
                },
            ],
        }
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            metadata_path = root / "metadata.psv"
            metadata_path.write_text("query_id|category|freshness_expectation|evidence_sufficient\nq1|configuration|none|true\nq2|debugging|current|true\n", encoding="utf-8")
            result = analyze(benchmark, read_metadata(metadata_path))
            self.assertEqual(result["categoryCount"], 2)
            self.assertEqual(len(result["categorySummaries"]), 4)
            self.assertEqual(result["failures"][0]["failureReason"], "ranking_regression")
            write_outputs(result, root / "out.json", root / "out.csv", root / "out.md")
            self.assertIn("Category and failure analysis", (root / "out.md").read_text(encoding="utf-8"))
            self.assertTrue((root / "out.csv").exists())


if __name__ == "__main__":
    unittest.main()
