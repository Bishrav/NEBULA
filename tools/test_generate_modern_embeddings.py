import tempfile
import unittest
from pathlib import Path

from generate_modern_embeddings import load_texts


class ModernEmbeddingInputTest(unittest.TestCase):
    def test_rejects_duplicate_texts(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "inputs.jsonl"
            path.write_text('{"text":"same"}\n{"text":"same"}\n', encoding="utf-8")
            with self.assertRaises(ValueError):
                load_texts(path)

    def test_accepts_nonempty_unique_texts(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "inputs.jsonl"
            path.write_text('{"text":"one"}\n{"text":"two"}\n', encoding="utf-8")
            self.assertEqual(["one", "two"], load_texts(path))


if __name__ == "__main__":
    unittest.main()
