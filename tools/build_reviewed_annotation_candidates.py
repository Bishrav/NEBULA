#!/usr/bin/env python3
"""Build a deterministic four-item candidate pool for reviewed-query annotation.

This is not a retrieval run: it selects one grounding document and seeded
same-project distractors for a blinded annotation packet.
"""

import argparse
import csv
import hashlib
import json
import random
from pathlib import Path


FIELDS = [
    "query_id", "annotation_query_id", "document_id", "retrieval_rank",
    "title", "excerpt", "score", "source_path", "source_url",
]


def excerpt(text, limit=1200):
    return " ".join(text.split()[:limit])


def annotation_id(query_id):
    digest = hashlib.sha256(query_id.encode("utf-8")).hexdigest()[:12].upper()
    return f"AQ-{digest}"


def build(draft_path, manifest_path, output_path, seed=20261102, distractors=3):
    draft = json.loads(draft_path.read_text(encoding="utf-8"))
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    corpus_root = manifest_path.parent.parent
    documents = {doc["document_id"]: doc for doc in manifest["documents"]}
    by_project = {}
    for doc in documents.values():
        by_project.setdefault(doc["project"], []).append(doc)
    for values in by_project.values():
        values.sort(key=lambda doc: doc["document_id"])

    rows = []
    for query in draft["queries"]:
        grounding_id = query["grounding_document_id"]
        grounding = documents.get(grounding_id)
        if grounding is None:
            raise ValueError(f"unknown grounding document: {grounding_id}")
        pool = [doc for doc in by_project[grounding["project"]] if doc["document_id"] != grounding_id]
        rng = random.Random(f"{seed}|{query['query_id']}")
        rng.shuffle(pool)
        selected = [grounding] + pool[:distractors]
        rng.shuffle(selected)
        for rank, doc in enumerate(selected, 1):
            text = (corpus_root / doc["text_path"]).read_text(encoding="utf-8")
            rows.append({
                "query_id": query["query_id"],
                "annotation_query_id": annotation_id(query["query_id"]),
                "document_id": doc["document_id"],
                # Kept for packet-schema compatibility; this is candidate
                # position, not a ranking-system result.
                "retrieval_rank": rank,
                "title": doc["title"],
                "excerpt": excerpt(text),
                "score": "",
                "source_path": doc["text_path"],
                "source_url": doc["source_url"],
            })
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with output_path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=FIELDS, delimiter="|", lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)
    return {"query_count": len(draft["queries"]), "candidate_pair_count": len(rows), "status": "PRIVATE_CANDIDATE_POOL"}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--draft", type=Path, required=True)
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--seed", type=int, default=20261102)
    parser.add_argument("--distractors", type=int, default=3)
    args = parser.parse_args()
    if args.distractors < 1:
        raise ValueError("distractors must be positive")
    print(json.dumps(build(args.draft, args.manifest, args.output, args.seed, args.distractors), sort_keys=True))


if __name__ == "__main__":
    raise SystemExit(main())
