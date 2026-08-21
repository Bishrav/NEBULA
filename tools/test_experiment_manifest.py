#!/usr/bin/env python3
"""Small regression test for experiment manifest generation and verification."""

import json
import tempfile
from pathlib import Path

from generate_experiment_manifest import build_manifest, verify


def main():
    with tempfile.TemporaryDirectory() as directory:
        root = Path(directory)
        source = root / "corpus.txt"
        source.write_text("stable corpus\n", encoding="utf-8")
        manifest_path = root / "manifest.json"
        manifest_path.write_text(json.dumps(build_manifest(root, {"corpus": source}, "2026-08-21T00:00:00+00:00")), encoding="utf-8")
        assert verify(manifest_path, root)["status"] == "verified"
        source.write_text("changed corpus\n", encoding="utf-8")
        try:
            verify(manifest_path, root)
        except ValueError:
            print("test_experiment_manifest: PASS")
            return
        raise AssertionError("changed input was not detected")


if __name__ == "__main__":
    main()
