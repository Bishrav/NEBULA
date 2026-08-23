import tempfile
import unittest
from pathlib import Path

from check_final_evaluation_gate import evaluate, qrels_ready, split_ready


class FinalEvaluationGateTest(unittest.TestCase):
    def test_missing_human_artifacts_block_gate(self):
        root = Path(tempfile.mkdtemp())
        corpus = root / "corpus.json"
        corpus.write_text(json_text({"documents": [{"document_id": str(i)} for i in range(500)]}), encoding="utf-8")
        stats = root / "metadata" / "corpus-statistics.json"
        stats.parent.mkdir()
        stats.write_text(json_text({"provisional": False}), encoding="utf-8")
        ranking = root / "ranking.json"
        ranking.write_text(json_text({"purpose": "final"}), encoding="utf-8")
        embedding = root / "embedding.json"
        embedding.write_text(json_text({"evidenceStatus": "FROZEN"}), encoding="utf-8")
        statistical = root / "statistics.md"
        statistical.write_text("protocol", encoding="utf-8")
        result = evaluate(root, corpus, root / "approved.psv", root / "qrels.psv", root / "split.json", ranking, embedding, statistical)
        self.assertEqual("BLOCKED", result["overall"])
        self.assertTrue(any(item["name"] == "human qrels and agreement release" and item["status"] == "BLOCKED" for item in result["checks"]))

    def test_qrels_require_release_sidecar(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "qrels.psv"
            path.write_text("query_id,document_id,relevance_grade\nq1,d1,3\n", encoding="utf-8")
            self.assertEqual((False, "qrels release sidecar is absent"), qrels_ready(path))

    def test_split_rejects_overlap(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "split.json"
            path.write_text(json_text({"frozen": True, "heldoutFlagRequired": "--final-heldout-evaluation", "development": {"queryIds": ["q1"]}, "validation": {"queryIds": ["q1"]}, "test": {"queryIds": ["q2"]}}), encoding="utf-8")
            self.assertEqual((False, "development, validation, and test query IDs must be disjoint"), split_ready(path))


def json_text(value):
    import json
    return json.dumps(value)


if __name__ == "__main__":
    unittest.main()
