import tempfile
import unittest
from pathlib import Path

from analyze_canonical_annotation_agreement import analyze


FIELDS = "annotator_code|query_id|document_id|grade|uncertainty|evidence_note|annotated_at\n"


class CanonicalAgreementTest(unittest.TestCase):
    def test_reports_disagreement(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            a = root / "a.psv"
            b = root / "b.psv"
            a.write_text(FIELDS + "A|q1|d1|3|false|direct|2026-11-02T10:00:00Z\nA|q1|d2|0|false|none|2026-11-02T10:00:00Z\n", encoding="utf-8")
            b.write_text(FIELDS + "B|q1|d1|2|false|partial|2026-11-02T10:00:00Z\nB|q1|d2|0|false|none|2026-11-02T10:00:00Z\n", encoding="utf-8")
            report = analyze([a, b])
            self.assertEqual(report["annotators"], ["A", "B"])
            self.assertEqual(report["disagreementCount"], 1)
            self.assertEqual(report["pairs"][0]["overlap"], 2)


if __name__ == "__main__":
    unittest.main()
