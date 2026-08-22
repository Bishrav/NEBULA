#!/usr/bin/env python3
"""Legacy synthetic-fixture packet generator.

This module is retained for existing regression tests. It injects grounding
documents and must not be used for the frozen research annotation stage. Use
generate_blinded_annotation_packages.py for the research pipeline.
"""

import argparse
import csv
import hashlib
import json
import random
import re
from pathlib import Path


def rows(path):
    with path.open(encoding="utf-8", newline="") as handle:
        return list(csv.DictReader(handle, delimiter="|"))


def excerpt(text, limit=1200):
    text = re.sub(r"\{\{<.*?>\}\}", "", text)
    words = text.split()
    return " ".join(words[:limit])


def build_packets(query_path, manifest_path, output_dir, annotators, seed, candidates_per_query):
    queries = rows(query_path)
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    corpus_root = manifest_path.parent.parent if manifest_path.parent.name == "manifests" else manifest_path.parent
    documents = manifest["documents"]
    by_id = {doc["document_id"]: doc for doc in documents}
    by_project = {}
    for doc in documents:
        by_project.setdefault(doc["project"], []).append(doc)
    for docs in by_project.values(): docs.sort(key=lambda doc: doc["document_id"])
    output_dir.mkdir(parents=True, exist_ok=True)
    packet_paths = []
    for annotator in annotators:
        packet_items = []
        for query in queries:
            grounding = by_id.get(query["grounding_document_id"])
            if grounding is None: raise ValueError(f"unknown grounding document: {query['grounding_document_id']}")
            pool = by_project.get(query["project"], documents)
            distractors = [doc for doc in pool if doc["document_id"] != grounding["document_id"]]
            randomizer = random.Random(int(hashlib.sha256(f"{seed}|{annotator}|{query['query_id']}".encode()).hexdigest()[:16], 16))
            randomizer.shuffle(distractors)
            selected = [grounding] + distractors[:max(0, candidates_per_query - 1)]
            randomizer.shuffle(selected)
            candidates = []
            for position, doc in enumerate(selected, 1):
                text = (corpus_root / doc["text_path"]).read_text(encoding="utf-8")
                candidates.append({"candidatePosition": position, "documentId": doc["document_id"],
                                   "project": doc["project"], "title": doc["title"], "excerpt": excerpt(text),
                                   "sourceUrl": doc["source_url"]})
            packet_items.append({"queryId": query["query_id"], "queryText": query["query_text"],
                                 "category": query["category"], "candidates": candidates})
        packet = {"schemaVersion": "blinded-annotation-packet-v1", "annotatorCode": annotator,
                  "seed": seed, "candidatesPerQuery": candidates_per_query,
                  "instructions": "Assign grade 0..3 independently. Do not use ranking scores, other labels, or hidden metadata. Record uncertainty and an evidence note.",
                  "items": packet_items}
        path = output_dir / f"packet-{annotator}.json"
        path.write_text(json.dumps(packet, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        template = output_dir / f"annotations-{annotator}.template.psv"
        template.write_text("annotator_code|query_id|document_id|grade|uncertainty|evidence_note|annotated_at\n", encoding="utf-8")
        packet_paths.append(str(path))
    return packet_paths


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--queries", type=Path, required=True)
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--annotators", default="A01,A02")
    parser.add_argument("--seed", type=int, default=20260822)
    parser.add_argument("--candidates-per-query", type=int, default=4)
    args = parser.parse_args()
    paths = build_packets(args.queries, args.manifest, args.output_dir, [item.strip() for item in args.annotators.split(",") if item.strip()], args.seed, args.candidates_per_query)
    print(json.dumps({"status": "created", "packets": paths, "human_annotation": "REQUIRED"}, sort_keys=True)); return 0


if __name__ == "__main__": raise SystemExit(main())
