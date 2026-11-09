import json
import tempfile
import unittest
from pathlib import Path

from analyze_authority_review_agreement import compare
from build_authority_review_template import build


class AuthorityReviewAgreementTest(unittest.TestCase):
    def test_compares_completed_reviews_and_flags_disagreements(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest = root / "manifest.json"
            manifest.write_text(json.dumps({"documents": [
                {"document_id": "d1", "text_path": "normalized/d1.md", "project": "Kubernetes", "source_url": "https://example.test/d1", "authority_category": "official"},
                {"document_id": "d2", "text_path": "normalized/d2.md", "project": "PostgreSQL", "source_url": "https://example.test/d2", "authority_category": "official"},
            ]}), encoding="utf-8")
            a = root / "a.psv"
            b = root / "b.psv"
            build(manifest, a)
            build(manifest, b)
            for path, reviewer, scores in ((a, "A", (0.9, 0.5)), (b, "B", (0.9, 0.7))):
                lines = path.read_text(encoding="utf-8").splitlines()
                rows = [line.split("|") for line in lines if line and not line.startswith("#")][1:]
                header = next(line for line in lines if line and not line.startswith("#"))
                updated = [header]
                for row, score in zip(rows, scores):
                    row[5] = str(score)
                    row[6] = "source evidence"
                    row[7] = reviewer
                    row[8] = "2026-11-09T00:00:00Z"
                    row[9] = "REVIEWED"
                    updated.append("|".join(row))
                path.write_text("\n".join([line for line in lines if line.startswith("#")] + updated) + "\n", encoding="utf-8")
            result = compare(manifest, a, b, 0.05)
            self.assertEqual(result["documents"], 2)
            self.assertEqual(result["disagreementCount"], 1)
            self.assertTrue(result["adjudicationRequired"])


if __name__ == "__main__":
    unittest.main()
