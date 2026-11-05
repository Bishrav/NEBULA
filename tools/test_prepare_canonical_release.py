import json
import tempfile
import unittest
from pathlib import Path

from prepare_canonical_release import build


FIELDS = "annotator_code|query_id|document_id|grade|uncertainty|evidence_note|annotated_at\n"


class CanonicalReleaseTest(unittest.TestCase):
    def test_builds_consensus_items(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            a = root / "a.psv"
            b = root / "b.psv"
            body_a = "A|q1|d1|3|false|direct|2026-11-05T10:00:00Z\n"
            body_b = "B|q1|d1|3|false|direct|2026-11-05T10:00:00Z\n"
            a.write_text(FIELDS + body_a, encoding="utf-8")
            b.write_text(FIELDS + body_b, encoding="utf-8")
            agreement = root / "agreement.json"
            agreement.write_text(json.dumps({"disagreementCount": 0, "pairs": [{"cohensKappa": 1.0}]}), encoding="utf-8")
            packet = build([a, b], agreement)
            self.assertEqual(len(packet["items"]), 1)
            self.assertEqual(packet["items"][0]["adjudicatedGrade"], 3)


if __name__ == "__main__":
    unittest.main()
