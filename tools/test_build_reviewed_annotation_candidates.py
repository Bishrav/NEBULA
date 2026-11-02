import json
import tempfile
import unittest
from pathlib import Path

from build_reviewed_annotation_candidates import build


class ReviewedAnnotationCandidatesTest(unittest.TestCase):
    def test_builds_private_pool_with_grounding_and_distractor(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "manifests").mkdir()
            (root / "normalized").mkdir()
            for name, text in (("d1", "grounding evidence"), ("d2", "distractor evidence")):
                (root / "normalized" / f"{name}.md").write_text(text, encoding="utf-8")
            manifest = {
                "documents": [
                    {"document_id": "d1", "project": "Kubernetes", "text_path": "normalized/d1.md", "title": "One", "source_url": "https://example/one"},
                    {"document_id": "d2", "project": "Kubernetes", "text_path": "normalized/d2.md", "title": "Two", "source_url": "https://example/two"},
                ]
            }
            manifest_path = root / "manifests" / "manifest.json"
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            draft_path = root / "draft.json"
            draft_path.write_text(json.dumps({"queries": [{"query_id": "q1", "query_text": "Question?", "grounding_document_id": "d1"}]}), encoding="utf-8")
            output = root / "candidates.psv"
            report = build(draft_path, manifest_path, output, seed=7, distractors=1)
            self.assertEqual(report["candidate_pair_count"], 2)
            lines = output.read_text(encoding="utf-8").splitlines()
            self.assertEqual(len(lines), 3)
            self.assertTrue(lines[1].startswith("q1|AQ-"))


if __name__ == "__main__":
    unittest.main()
