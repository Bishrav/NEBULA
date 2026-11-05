import json
import tempfile
import unittest
from pathlib import Path

from build_modern_embedding_inputs import build, normalize_markdown


class ModernEmbeddingInputBuilderTest(unittest.TestCase):
    def test_matches_java_whitespace_behavior(self):
        self.assertEqual("step\u00a02", normalize_markdown("step\u00a02"))

    def test_preserves_pipe_characters_in_query_text(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            corpus = root / "corpus"
            corpus.mkdir()
            (corpus / "doc.md").write_text("A document", encoding="utf-8")
            queries = root / "queries.psv"
            queries.write_text("q1|How do || operators work?|doc.md:3\n", encoding="utf-8")

            rows = build(corpus, queries)

            query = next(row for row in rows if row["kind"] == "query")
            self.assertEqual("How do || operators work?", query["text"])


if __name__ == "__main__":
    unittest.main()
