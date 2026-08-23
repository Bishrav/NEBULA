import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from run_distributed_experiment import baseline_result_ids, counter_deltas, ndcg, percentile, top_k_overlap


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

    def test_baseline_reads_first_observation_from_summary(self):
        import json
        import tempfile
        with tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False) as handle:
            json.dump({"observations": [{"resultIds": ["a", "b"]}]}, handle)
            path = __import__("pathlib").Path(handle.name)
        self.assertEqual(["a", "b"], baseline_result_ids(path))
        path.unlink()

    def test_counter_deltas_do_not_report_cumulative_values(self):
        self.assertEqual({"requestAdmissions": 2, "retryAttempts": 1, "successfulShardRequests": 2, "shardFailures": 1},
                         counter_deltas({"requestAdmissions": 10, "retryAttempts": 4, "successfulShardRequests": 9, "shardFailures": 2},
                                        {"requestAdmissions": 8, "retryAttempts": 3, "successfulShardRequests": 7, "shardFailures": 1}))


if __name__ == "__main__":
    unittest.main()
