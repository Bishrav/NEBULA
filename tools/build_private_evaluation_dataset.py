#!/usr/bin/env python3
"""Join protected qrels to frozen query text for the private Java evaluator input."""

import argparse
import csv
import json
from pathlib import Path


def load_queries(path):
    with path.open(encoding="utf-8", newline="") as handle:
        rows = list(csv.DictReader(handle, delimiter="|"))
    if not rows or rows[0].get("query_id") is None or rows[0].get("query_text") is None:
        raise ValueError("query file must contain query_id and query_text")
    return {row["query_id"]: row["query_text"] for row in rows}


def load_qrels(path):
    with path.open(encoding="utf-8-sig", newline="") as handle:
        rows = list(csv.DictReader(handle, delimiter="|"))
    required = {"query_id", "document_id", "relevance_grade"}
    if not rows or set(rows[0]) != required:
        raise ValueError("qrels schema mismatch")
    result = {}
    for row in rows:
        key = (row["query_id"], row["document_id"])
        if key in result:
            raise ValueError("duplicate qrels pair: " + repr(key))
        grade = int(row["relevance_grade"])
        if grade not in range(4):
            raise ValueError("qrels grade must be 0..3")
        result.setdefault(row["query_id"], []).append((row["document_id"], grade))
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--queries", type=Path, required=True)
    parser.add_argument("--qrels", type=Path, required=True)
    parser.add_argument("--corpus-manifest", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    queries = load_queries(args.queries)
    qrels = load_qrels(args.qrels)
    manifest = json.loads(args.corpus_manifest.read_text(encoding="utf-8"))
    paths = {row["document_id"]: row["text_path"] for row in manifest["documents"]}
    if set(queries) != set(qrels):
        raise ValueError("query and qrels IDs do not match")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", encoding="utf-8", newline="\n") as handle:
        handle.write("query_id|query_text|judgements\n")
        for query_id in sorted(queries):
            judgments = []
            for document_id, grade in qrels[query_id]:
                if document_id not in paths:
                    raise ValueError("qrels document is absent from corpus manifest: " + document_id)
                judgments.append(paths[document_id] + ":" + str(grade))
            handle.write(query_id + "|" + queries[query_id] + "|" + ";".join(judgments) + "\n")
    print(json.dumps({"status": "created", "queries": len(queries), "judgements": sum(map(len, qrels.values())), "output": str(args.output)}))


if __name__ == "__main__":
    main()
