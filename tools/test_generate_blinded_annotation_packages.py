import csv
import json
import tempfile
import unittest
import zipfile
from pathlib import Path

from generate_blinded_annotation_packages import build_packages


class BlindedAnnotationPackagesTest(unittest.TestCase):
    def test_packages_share_pairs_but_exclude_private_artifacts(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            candidates = root / "candidates.psv"
            candidates.write_text(
                "query_id|annotation_query_id|document_id|retrieval_rank|title|excerpt|score|source_path|source_url\n"
                "q1|AQ-0001|d1|1|One|Evidence one|0.9|one.md|https://example/one\n"
                "q1|AQ-0001|d2|2|Two|Evidence two|0.8|two.md|https://example/two\n", encoding="utf-8")
            queries = root / "queries.psv"
            queries.write_text("query_id|query_text\nq1|How do I configure this system?\n", encoding="utf-8")
            manifest = build_packages(candidates, queries, root / "private", root / "packages", 7, 10)
            self.assertTrue(manifest["privateMappingExcludedFromPackages"])
            with zipfile.ZipFile(root / "packages" / "blinded-annotation-package-A.zip") as archive:
                text = archive.read("annotator-A/annotation-packet-01.psv").decode("utf-8")
                self.assertNotIn("score", text)
                self.assertNotIn("PRIVATE", "\n".join(archive.namelist()))
            with zipfile.ZipFile(root / "packages" / "blinded-annotation-package-B.zip") as archive:
                self.assertEqual(
                    ["annotator-B/annotation-packet-01.psv", "ANNOTATION-RUBRIC.md", "README.md"],
                    archive.namelist(),
                )


if __name__ == "__main__":
    unittest.main()
