import csv
import json
import tempfile
import unittest
from pathlib import Path

from build_authority_review_template import build


class AuthorityReviewTemplateTest(unittest.TestCase):
    def test_template_preserves_provenance_and_leaves_decisions_blank(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest = root / "manifest.json"
            manifest.write_text(json.dumps({"documents": [{
                "document_id": "d1", "text_path": "normalized/d1.md", "project": "Kubernetes",
                "source_url": "https://example.test/d1", "authority_category": "official-project-documentation",
            }]}), encoding="utf-8")
            output = root / "authority-review.psv"

            result = build(manifest, output)

            self.assertEqual(result["documents"], 1)
            lines = output.read_text(encoding="utf-8").splitlines()
            self.assertIn("corpusManifestSha256=", lines[0])
            row = next(csv.DictReader((line for line in lines if not line.startswith("#")), delimiter="|"))
            self.assertEqual(row["source_path"], "normalized/d1.md")
            self.assertEqual(row["current_authority_category"], "official-project-documentation")
            self.assertEqual(row["proposed_authority"], "")
            self.assertEqual(row["review_status"], "UNREVIEWED")


if __name__ == "__main__":
    unittest.main()
