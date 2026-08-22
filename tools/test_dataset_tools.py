#!/usr/bin/env python3
"""Regression tests for corpus provenance and deterministic query splitting."""

import hashlib
import json
import tempfile
from pathlib import Path

from split_query_set import main as split_main
from validate_corpus_manifest import validate


def main():
    with tempfile.TemporaryDirectory() as directory:
        root = Path(directory)
        corpus = root / "corpus"
        corpus.mkdir()
        text = corpus / "docs/a.md"
        text.parent.mkdir()
        text.write_text("# Public document\n", encoding="utf-8")
        digest = hashlib.sha256(text.read_bytes()).hexdigest()
        manifest = root / "manifest.psv"
        manifest.write_text(
            "document_id|title|source|source_type|project|created_at|updated_at|version|authority_category|source_url|graph_links|text_path|sha256|license|attribution\n"
            f"doc-a|Public document|Example|README|example|2026-01-01|2026-01-02|v1|maintainer|https://example.com/a||docs/a.md|{digest}|MIT|Example project\n",
            encoding="utf-8")
        assert validate(manifest, corpus)["documents"] == 1

        queries = root / "queries.psv"
        queries.write_text("query_id|query|judgments\n" + "\n".join(
            f"q{i}|question {i}|docs/a.md:3" for i in range(1, 11)) + "\n", encoding="utf-8")
        output = root / "splits"
        import sys
        original = sys.argv
        sys.argv = ["split_query_set.py", "--queries", str(queries), "--output-dir", str(output),
                    "--seed", "7", "--corpus-version", "corpus-test", "--code-revision", "test"]
        try:
            assert split_main() == 0
        finally:
            sys.argv = original
        manifest_data = json.loads((output / "split-manifest.json").read_text(encoding="utf-8"))
        assert sum(manifest_data["counts"].values()) == 10
        assert len(manifest_data["assignments"]) == 10
        assert len(set(manifest_data["assignments"].values())) == 3
        print("test_dataset_tools: PASS")


if __name__ == "__main__":
    main()
