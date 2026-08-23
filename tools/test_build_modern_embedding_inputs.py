import tempfile
import unittest
from pathlib import Path

from build_modern_embedding_inputs import build, normalize_markdown


class ModernEmbeddingInputsTest(unittest.TestCase):
    def test_normalization_matches_ingestion_contract(self):
        self.assertEqual("Title linked item", normalize_markdown("# Title\n\n- [linked item](doc.md)"))

    def test_builds_unique_deterministic_rows(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); corpus = root / "corpus"; corpus.mkdir()
            (corpus / "a.md").write_text("same", encoding="utf-8")
            (corpus / "b.md").write_text("same", encoding="utf-8")
            queries = root / "queries.psv"
            queries.write_text("query_id|query_text|judgements\nq1|same|a.md:3\nq2|different|b.md:2\n", encoding="utf-8")
            rows = build(corpus, queries)
            self.assertEqual(["document", "query"], [row["kind"] for row in rows])
            self.assertEqual(["same", "different"], [row["text"] for row in rows])


if __name__ == "__main__": unittest.main()
