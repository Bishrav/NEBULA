#!/usr/bin/env python3
"""Build deterministic unique text inputs for the offline modern embedding cache."""

import argparse
import hashlib
import json
import re
from pathlib import Path


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def normalize_markdown(raw):
    """Mirror DocumentIngestor.normalizeMarkdown for cache-key compatibility."""
    text = raw.replace("\r\n", "\n").replace("\r", "\n")
    text = re.sub(r"\[([^]]+)]\([^)]*\)", r"\1", text)
    text = re.sub(r"<[^>]+>", " ", text)
    text = re.sub(r"^\s*[-*+]\s+", "", text)
    text = re.sub(r"^\s*\d+[.)]\s+", "", text)
    text = re.sub(r"^#{1,6}\s*", "", text)
    text = re.sub(r"[*_~`]", "", text)
    text = text.replace("|", " ")
    # Match Java's default Pattern \s behavior; do not fold Unicode NBSP into ASCII space.
    return re.sub(r"[ \t\n\x0b\f\r]+", " ", text).strip()


def build(corpus, queries):
    rows, seen = [], set()
    for path in sorted(corpus.rglob("*.md")):
        text = normalize_markdown(path.read_text(encoding="utf-8"))
        if text not in seen:
            seen.add(text); rows.append({"kind": "document", "id": path.relative_to(corpus).as_posix(), "text": text})
    lines = queries.read_text(encoding="utf-8").splitlines()
    header = next((line for line in lines if line.strip() and not line.startswith("#")), "")
    header_columns = header.split("|")
    has_header = len(header_columns) >= 2 and header_columns[0:2] == ["query_id", "query_text"]
    query_text_column = header_columns.index("query_text") if has_header else 1
    for line in lines:
        if not line.strip() or line.startswith("#") or (has_header and line == header): continue
        parts = line.split("|")
        if has_header:
            if len(parts) != len(header_columns): raise ValueError("malformed query row")
        elif len(parts) < 3:
            raise ValueError("queries must have a header or query_id|query_text|judgements rows")
        query_id = parts[0].strip()
        text = (parts[query_text_column] if has_header else "|".join(parts[1:-1])).strip()
        if not query_id or not text: raise ValueError("malformed query row")
        if text not in seen:
            seen.add(text); rows.append({"kind": "query", "id": query_id, "text": text})
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
