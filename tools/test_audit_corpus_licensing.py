#!/usr/bin/env python3
import json
import tempfile
import unittest
from pathlib import Path

from audit_corpus_licensing import audit


class CorpusLicenseAuditTest(unittest.TestCase):
    def test_audit_requires_human_review_and_reports_missing_notices(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "licenses").mkdir()
            registry = root / "registry.psv"
            registry.write_text(
                "# header\n"
                "source_id|project|upstream_repository|scope|source_url|license|attribution|license_evidence_url|status|notes\n"
                "source-example|Example|example/project|docs/**|https://github.com/example/project/tree/main/docs|MIT|Example Authors|https://github.com/example/project/blob/main/LICENSE|ACCEPTED|review\n",
                encoding="utf-8",
            )
            manifest = root / "manifest.json"
            manifest.write_text(json.dumps({
                "corpus_version": "research-corpus-v1",
                "documents": [{"project": "Example"}],
            }), encoding="utf-8")
            report = audit(registry, manifest, root)
            self.assertEqual(report["structural_status"], "REVIEW_REQUIRED")
            self.assertEqual(report["legal_clearance"], "NOT_DECIDED")
            self.assertTrue(report["human_review_required"])
            self.assertIn("retained upstream notice file not found under licenses/", report["structural_errors"][0])

    def test_audit_passes_when_notice_is_retained(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "licenses").mkdir()
            (root / "licenses" / "source-example-LICENSE.txt").write_text("MIT License\n", encoding="utf-8")
            registry = root / "registry.psv"
            registry.write_text(
                "source_id|project|upstream_repository|scope|source_url|license|attribution|license_evidence_url|status|notes\n"
                "source-example|Example|example/project|docs/**|https://github.com/example/project/tree/main/docs|MIT|Example Authors|https://github.com/example/project/blob/main/LICENSE|ACCEPTED|review\n",
                encoding="utf-8",
            )
            manifest = root / "manifest.json"
            manifest.write_text(json.dumps({
                "corpus_version": "research-corpus-v1",
                "documents": [{"project": "Example"}],
            }), encoding="utf-8")
            report = audit(registry, manifest, root)
            self.assertEqual(report["structural_status"], "PASS")
            self.assertEqual(report["sources"][0]["retained_notice_files"], ["licenses/source-example-LICENSE.txt"])


if __name__ == "__main__":
    unittest.main()
