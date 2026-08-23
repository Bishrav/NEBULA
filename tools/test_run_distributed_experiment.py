import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from run_distributed_experiment import ndcg, percentile, top_k_overlap


class DistributedExperimentTest(unittest.TestCase):
    def test_overlap_is_bounded_and_uses_k(self):
        self.assertEqual(2 / 3, top_k_overlap(["a", "b", "c"], ["b", "c", "d"], 3))
        self.assertEqual(1.0, top_k_overlap(["a", "b"], ["b", "a"], 2))

    def test_percentiles_are_deterministic(self):
        values = [5, 1, 3, 2, 4]
        self.assertEqual(3, percentile(values, 0.50))
        self.assertEqual(5, percentile(values, 0.99))

    def test_ndcg_is_bounded(self):
        relevance = {"a": 3, "b": 1, "c": 0}
        self.assertEqual(1.0, ndcg(["a", "b", "c"], relevance, 3))
        self.assertLess(ndcg(["c", "b", "a"], relevance, 3), 1.0)


if __name__ == "__main__":
    unittest.main()
