import tempfile
import unittest
import zipfile
from pathlib import Path

from validate_annotation_submission import validate

FIELDS = "annotation_query_id|query_text|document_id|document_title|document_excerpt|relevance_grade_0_3|uncertainty|evidence_note|annotated_at\n"


class ValidateAnnotationSubmissionTest(unittest.TestCase):
    def test_valid_submission_is_normalized(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            packet = root / "packet.zip"
            with zipfile.ZipFile(packet, "w") as archive:
                archive.writestr("packet.psv", FIELDS + "AQ-0001|How?|d1|Title|Excerpt||||\n")
            submission = root / "submission.zip"
            completed = FIELDS + "AQ-0001|How?|d1|Title|Excerpt|3|false|Direct evidence|2026-08-22T12:00:00+00:00\n"
            with zipfile.ZipFile(submission, "w") as archive:
                archive.writestr("packet.psv", completed)
            key = root / "key.psv"
            key.write_text("annotation_query_id|original_query_id\nAQ-0001|q1\n", encoding="utf-8")
            output = root / "canonical.psv"
            result = validate(submission, packet, key, "A", output)
            self.assertEqual(1, result["rows"])
            self.assertIn("A|q1|d1|3|false", output.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
