#!/usr/bin/env python3
"""Build deterministic unique text inputs for the offline modern embedding cache."""

import argparse
import hashlib
import json
from pathlib import Path


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def build(corpus, queries):
    rows, seen = [], set()
    for path in sorted(corpus.rglob("*.md")):
        text = path.read_text(encoding="utf-8")
        if text not in seen:
            seen.add(text); rows.append({"kind": "document", "id": path.relative_to(corpus).as_posix(), "text": text})
    lines = queries.read_text(encoding="utf-8").splitlines()
    for line in lines:
        if not line.strip() or line.startswith("#") or line.startswith("query_id|"): continue
        parts = line.split("|", 2)
        if len(parts) < 2 or not parts[0].strip() or not parts[1].strip(): raise ValueError("malformed query row")
        text = parts[1].strip()
        if text not in seen:
            seen.add(text); rows.append({"kind": "query", "id": parts[0].strip(), "text": text})
    if not rows: raise ValueError("no corpus or query texts found")
    return rows


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--corpus", type=Path, required=True)
    parser.add_argument("--queries", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--manifest", type=Path, required=True)
    args = parser.parse_args()
    if not args.corpus.is_dir() or not args.queries.is_file(): raise ValueError("corpus and queries must exist")
    rows = build(args.corpus, args.queries)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", encoding="utf-8", newline="\n") as handle:
        for row in rows: handle.write(json.dumps(row, ensure_ascii=False, sort_keys=True) + "\n")
    args.manifest.parent.mkdir(parents=True, exist_ok=True)
    args.manifest.write_text(json.dumps({"schemaVersion": "modern-embedding-inputs-v1", "rows": len(rows), "inputSha256": {"queries": sha256(args.queries)}, "status": "READY_FOR_PINNED_MODEL_INFERENCE"}, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"status": "created", "rows": len(rows), "output": str(args.output)}, sort_keys=True))


if __name__ == "__main__": main()
