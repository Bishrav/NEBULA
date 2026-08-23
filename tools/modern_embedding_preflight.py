#!/usr/bin/env python3
"""Check whether the pinned modern-embedding environment is available."""

import argparse
import importlib.util
import json
import platform
import sys
from pathlib import Path

from generate_modern_embeddings import MODEL_ID, MODEL_LICENSE, MODEL_REVISION


def check(requirements):
    packages = {}
    for line in Path(requirements).read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        package = line.split("==", 1)[0].strip()
        import_name = "sentence_transformers" if package == "sentence-transformers" else package
        packages[package] = {"importName": import_name, "installed": importlib.util.find_spec(import_name) is not None}
    ready = all(item["installed"] for item in packages.values())
    return {
        "schemaVersion": "modern-embedding-preflight-v1",
        "status": "READY" if ready else "ENVIRONMENT_LIMITED",
        "python": platform.python_version(),
        "platform": platform.platform(),
        "modelId": MODEL_ID,
        "revision": MODEL_REVISION,
        "license": MODEL_LICENSE,
        "requirements": packages,
        "nextAction": "Run pip install -r tools/modern-embeddings-requirements.txt in a compatible isolated environment" if not ready else "Run tools/generate_modern_embeddings.py with the frozen input manifest",
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--requirements", type=Path, default=Path("tools/modern-embeddings-requirements.txt"))
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    result = check(args.requirements)
    serialized = json.dumps(result, indent=2, sort_keys=True) + "\n"
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(serialized, encoding="utf-8")
    print(serialized, end="")
    return 0 if result["status"] == "READY" else 2


if __name__ == "__main__":
    raise SystemExit(main())
