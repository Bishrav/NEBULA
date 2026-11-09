import json
import tempfile
import unittest
from pathlib import Path

from build_authority_review_template import build
from validate_authority_review import validate


class ValidateAuthorityReviewTest(unittest.TestCase):
    def test_rejects_unreviewed_template(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest = root / "manifest.json"
            manifest.write_text(json.dumps({"documents": [{
                "document_id": "d1", "text_path": "normalized/d1.md", "project": "Kubernetes",
                "source_url": "https://example.test/d1", "authority_category": "official",
            }]}), encoding="utf-8")
            review = root / "review.psv"
            build(manifest, review)
            with self.assertRaisesRegex(ValueError, "not reviewed"):
                validate(manifest, review)

    def test_accepts_complete_review_and_reports_variation(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest = root / "manifest.json"
            manifest.write_text(json.dumps({"documents": [
                {"document_id": "d1", "text_path": "normalized/d1.md", "project": "Kubernetes", "source_url": "https://example.test/d1", "authority_category": "official"},
                {"document_id": "d2", "text_path": "normalized/d2.md", "project": "PostgreSQL", "source_url": "https://example.test/d2", "authority_category": "official"},
            ]}), encoding="utf-8")
            review = root / "review.psv"
            build(manifest, review)
            lines = review.read_text(encoding="utf-8").replace("|||||UNREVIEWED", "|0.9|source evidence|reviewer|2026-11-09T00:00:00Z|REVIEWED", 1)
            lines = lines.replace("|||||UNREVIEWED", "|0.6|source evidence|reviewer|2026-11-09T00:00:00Z|REVIEWED", 1)
            review.write_text(lines, encoding="utf-8")
            result = validate(manifest, review)
            self.assertTrue(result["hasAuthorityVariation"])
            self.assertEqual(result["documents"], 2)


if __name__ == "__main__":
    unittest.main()
