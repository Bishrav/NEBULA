#!/usr/bin/env python3
import hashlib
import json
import tempfile
import unittest
from pathlib import Path

from validate_research_corpus import validate


class ResearchCorpusValidationTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.root = Path(self.tmp.name)
        (self.root / "raw").mkdir()
        (self.root / "normalized").mkdir()
        (self.root / "raw/a.txt").write_text("source\n", encoding="utf-8")
        (self.root / "normalized/a.md").write_text("# Source\n", encoding="utf-8")

    def tearDown(self):
        self.tmp.cleanup()

    def manifest(self, **overrides):
        raw = (self.root / "raw/a.txt").read_bytes()
        normalized = (self.root / "normalized/a.md").read_bytes()
        doc = {"document_id": "kubernetes-a", "project": "Kubernetes", "title": "A",
               "source_url": "https://github.com/kubernetes/website/blob/" + "a" * 40 + "/a.md",
               "upstream_repository": "https://github.com/kubernetes/website", "upstream_commit": "a" * 40,
               "source_type": "official-documentation", "retrieved_at": "2026-08-22T00:00:00+00:00",
               "license": "CC-BY-4.0", "attribution": "The Kubernetes Authors",
               "content_checksum": hashlib.sha256(raw).hexdigest(), "normalized_checksum": hashlib.sha256(normalized).hexdigest(),
               "last_updated": None, "transformation_notes": "deterministic", "version": "v1",
               "authority_category": "official-project-documentation", "graph_links": [],
               "raw_path": "raw/a.txt", "text_path": "normalized/a.md", "word_count": 1}
        doc.update(overrides)
        return {"schema_version": "research-corpus-manifest-v1", "corpus_version": "research-corpus-v1", "documents": [doc]}

    def write(self, manifest):
        path = self.root / "manifest.json"
        path.write_text(json.dumps(manifest), encoding="utf-8")
        return path

    def test_valid_manifest_and_reproducibility(self):
        manifest = self.manifest()
        self.assertEqual(validate(self.write(manifest), self.root)["documents"], 1)
        self.assertEqual(manifest["documents"][0]["normalized_checksum"], manifest["documents"][0]["normalized_checksum"])

    def test_duplicate_ids_and_hashes_rejected(self):
        manifest = self.manifest()
        duplicate = dict(manifest["documents"][0])
        manifest["documents"].append(duplicate)
        with self.assertRaises(ValueError): validate(self.write(manifest), self.root)

    def test_missing_license_and_source_url_rejected(self):
        with self.assertRaises(ValueError): validate(self.write(self.manifest(license="")), self.root)
        with self.assertRaises(ValueError): validate(self.write(self.manifest(source_url="http://example.com")), self.root)

    def test_invalid_provenance_rejected(self):
        with self.assertRaises(ValueError): validate(self.write(self.manifest(upstream_commit="bad")), self.root)


if __name__ == "__main__":
    unittest.main()
