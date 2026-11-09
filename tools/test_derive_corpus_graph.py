import json
import tempfile
import unittest
from pathlib import Path

from derive_corpus_graph import derive


class DeriveCorpusGraphTest(unittest.TestCase):
    def test_resolves_admitted_links_and_reports_unresolved_targets(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifests = root / "manifests"
            normalized = root / "normalized"
            manifests.mkdir()
            normalized.mkdir()
            (normalized / "a.md").write_text(
                "See [B](/docs/b.md) and [missing](missing.md).\n", encoding="utf-8"
            )
            (normalized / "b.md").write_text("Target.\n", encoding="utf-8")
            manifest = {
                "documents": [
                    {"raw_path": "raw/source/content/en/docs/a.md", "text_path": "normalized/a.md"},
                    {"raw_path": "raw/source/content/en/docs/b.md", "text_path": "normalized/b.md"},
                ]
            }
            manifest_path = manifests / "manifest.json"
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            output = root / "graph.psv"

            result = derive(manifest_path, output)

            self.assertEqual(result["documents"], 2)
            self.assertEqual(result["matchedEdges"], 1)
            self.assertEqual(result["unresolvedRelativeLinks"], 1)
            self.assertIn("a.md|b.md", output.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
