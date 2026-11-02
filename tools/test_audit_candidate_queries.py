#!/usr/bin/env python3
import tempfile
import unittest
from pathlib import Path

from audit_candidate_queries import audit


class CandidateQueryAuditTest(unittest.TestCase):
    def test_mechanical_templates_are_flagged_without_approval(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "queries.psv"
            path.write_text(
                "query_id|query_text|category|project|difficulty|freshness_sensitive|authority_sensitive|exact_identifier_dependency|evidence_required|source_generation_method|grounding_document_id|grounding_excerpt_checksum|review_status\n"
                "q1|How should an engineer use the API or interface for Pod logs in Kubernetes?|API usage|Kubernetes|intermediate|True|True|True|True|deterministic heading paraphrase; human review required|doc-1|abc|NEEDS_HUMAN_REVIEW\n"
                "q2|How can an operator recover a failed database primary in PostgreSQL?|failure recovery|PostgreSQL|advanced|True|True|False|True|human-authored draft|doc-2|def|NEEDS_HUMAN_REVIEW\n",
                encoding="utf-8",
            )
            report = audit(path)
            self.assertEqual(report["candidate_count"], 2)
            self.assertEqual(report["flagged_count"], 1)
            self.assertIn("mechanical_api_template", report["queries"][0]["flags"])
            self.assertIn("generated_not_human_reviewed", report["queries"][0]["flags"])
            self.assertEqual(report["queries"][1]["flags"], [])

    def test_missing_grounding_is_flagged(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "queries.psv"
            path.write_text(
                "query_id|query_text|category|project|difficulty|freshness_sensitive|authority_sensitive|exact_identifier_dependency|evidence_required|source_generation_method|grounding_document_id|grounding_excerpt_checksum|review_status\n"
                "q1|How can an operator recover a failed database primary in PostgreSQL?|failure recovery|PostgreSQL|advanced|True|True|False|True|human-authored draft|||NEEDS_HUMAN_REVIEW\n",
                encoding="utf-8",
            )
            report = audit(path)
            self.assertIn("missing_grounding_document", report["queries"][0]["flags"])


if __name__ == "__main__":
    unittest.main()
