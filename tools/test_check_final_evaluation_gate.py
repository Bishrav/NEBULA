import tempfile
import unittest
from pathlib import Path

from check_final_evaluation_gate import evaluate


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


def json_text(value):
    import json
    return json.dumps(value)


if __name__ == "__main__":
    unittest.main()
