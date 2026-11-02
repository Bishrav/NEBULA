import tempfile
import unittest
from pathlib import Path

from generate_publication_package import generate


class PublicationPackageTest(unittest.TestCase):
    def test_outputs_are_complete_and_explicitly_limited(self):
        benchmark = {"cutoff": 5, "variants": [{"name": "bm25", "precisionAtK": 0.2, "recallAtK": 0.4, "mrr": 0.3, "ndcgAtK": 0.5, "deltaNdcgVsBm25": 0.0}]}
        ann = {"exactRecallAtK": 1.0, "exactMeanLatencyMs": 1.0, "hnswRecallAtK": 0.9, "hnswMeanLatencyMs": 2.0}
        distributed = [{"scenario": "D0", "status": "MEASURED", "throughputPerSecond": 1.0, "p50LatencyMillis": 2.0, "p95LatencyMillis": 3.0, "partialResultFrequency": 0.0, "ndcgDegradation": "NOT_MEASURED"}]
        paired = {"results": [{"variant": "semantic", "metric": "ndcgAtK", "n": 2, "meanDifference": 0.1, "medianDifference": 0.1, "bootstrap95PercentCI": [0.0, 0.2], "winRate": 0.5, "permutationPValue": 0.5, "holmAdjustedPValue": 0.5}]}
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            generate(benchmark, ann, distributed, output, paired)
            self.assertIn("REGRESSION_FIXTURE_ONLY", (output / "README.md").read_text(encoding="utf-8"))
            self.assertTrue((output / "ranking-comparison.tex").stat().st_size > 0)
            self.assertIn("<svg", (output / "ranking-ndcg.svg").read_text(encoding="utf-8"))
            self.assertTrue((output / "distributed-summary.csv").exists())
            self.assertTrue((output / "paired-ndcg-comparison.csv").exists())
            self.assertTrue((output / "paired-ndcg-comparison.tex").exists())
            self.assertIn("Paired NDCG differences", (output / "paired-ndcg-differences.svg").read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
