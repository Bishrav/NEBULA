#!/usr/bin/env python3
import csv
import json
import tempfile
import unittest
from pathlib import Path

from generate_blinded_annotation_packets import build_packets
from validate_blinded_annotations import validate
from build_final_qrels import build


class AnnotationWorkflowTest(unittest.TestCase):
    def test_packet_randomization_and_validation(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); (root / "normalized").mkdir()
            (root / "normalized/doc.md").write_text("# Evidence\nUse this procedure.", encoding="utf-8")
            checksum = "a" * 64
            manifest = root / "manifest.json"
            manifest.write_text(json.dumps({"documents": [{"document_id": "doc-a", "project": "Kubernetes", "title": "Evidence", "source_url": "https://example.com", "text_path": "normalized/doc.md", "normalized_checksum": checksum}]}), encoding="utf-8")
            queries = root / "queries.psv"
            queries.write_text("query_id|query_text|category|project|difficulty|freshness_sensitive|authority_sensitive|exact_identifier_dependency|evidence_required|source_generation_method|grounding_document_id|grounding_excerpt_checksum|review_status\nq1|How should an engineer verify this evidence in Kubernetes?|security|Kubernetes|intermediate|false|true|false|true|candidate|doc-a|" + checksum + "|NEEDS_HUMAN_REVIEW\n", encoding="utf-8")
            packet_dir = root / "packets"; build_packets(queries, manifest, packet_dir, ["A01", "A02"], 7, 1)
            packet_a = json.loads((packet_dir / "packet-A01.json").read_text(encoding="utf-8"))
            self.assertEqual(packet_a["annotatorCode"], "A01")
            annotation = packet_dir / "annotations-A01.psv"
            annotation.write_text("annotator_code|query_id|document_id|grade|uncertainty|evidence_note|annotated_at\nA01|q1|doc-a|3|false|direct evidence|2026-08-22T00:00:00+00:00\n", encoding="utf-8")
            self.assertEqual(validate(annotation, packet_dir / "packet-A01.json", "A01")["rows"], 1)

    def test_qrels_builder_requires_release_gate(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); release = root / "release.json"; packet = root / "packet.json"; output = root / "qrels.psv"
            release.write_text(json.dumps({"status": "blocked"}), encoding="utf-8")
            packet.write_text(json.dumps({"items": []}), encoding="utf-8")
            with self.assertRaises(ValueError): build(release, packet, output)


if __name__ == "__main__": unittest.main()
