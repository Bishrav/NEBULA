import tempfile
import unittest
from pathlib import Path

from generate_publication_package import generate


class PublicationPackageTest(unittest.TestCase):
    def test_outputs_are_complete_and_explicitly_limited(self):
        benchmark = {"cutoff": 5, "variants": [{"name": "bm25", "precisionAtK": 0.2, "recallAtK": 0.4, "mrr": 0.3, "ndcgAtK": 0.5, "deltaNdcgVsBm25": 0.0}]}
        ann = {"exactRecallAtK": 1.0, "exactMeanLatencyMs": 1.0, "hnswRecallAtK": 0.9, "hnswMeanLatencyMs": 2.0}
        distributed = [{"scenario": "D0", "status": "MEASURED", "throughputPerSecond": 1.0, "p50LatencyMillis": 2.0, "p95LatencyMillis": 3.0, "partialResultFrequency": 0.0, "ndcgDegradation": "NOT_MEASURED"}]
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            generate(benchmark, ann, distributed, output)
            self.assertIn("REGRESSION_FIXTURE_ONLY", (output / "README.md").read_text(encoding="utf-8"))
            self.assertTrue((output / "ranking-comparison.tex").stat().st_size > 0)
            self.assertIn("<svg", (output / "ranking-ndcg.svg").read_text(encoding="utf-8"))
            self.assertTrue((output / "distributed-summary.csv").exists())


if __name__ == "__main__":
    unittest.main()
