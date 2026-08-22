#!/usr/bin/env python3
import json
import tempfile
import unittest
from pathlib import Path

from analyze_blinded_agreement import analyze


class BlindedAgreementTest(unittest.TestCase):
    def test_disagreement_report(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); packet_paths = []
            for code, grade in (("A01", 3), ("A02", 2)):
                packet = root / f"packet-{code}.json"; packet.write_text(json.dumps({"annotatorCode": code, "items": [{"queryId": "q1", "candidates": [{"documentId": "d1"}]}]}), encoding="utf-8")
                annotation = root / f"annotation-{code}.psv"; annotation.write_text("annotator_code|query_id|document_id|grade|uncertainty|evidence_note|annotated_at\n" + f"{code}|q1|d1|{grade}|false|evidence|2026-08-22T00:00:00+00:00\n", encoding="utf-8")
                packet_paths.append(packet)
                if code == "A01": first = annotation
                else: second = annotation
            result = analyze([first, second], packet_paths)
            self.assertEqual(result["disagreementCount"], 1)


if __name__ == "__main__": unittest.main()
