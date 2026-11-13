#!/usr/bin/env python3
"""Build deterministic trust and graph inputs from a frozen corpus manifest."""

import argparse
import csv
import hashlib
import json
from datetime import datetime, timezone
from pathlib import Path


AUTHORITY = {"official-project-documentation": 1.0}


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load_reviewed_categories(manifest_path, reviews_dir):
    manifest_sha = sha256(manifest_path)
    expected = {document["document_id"] for document in json.loads(manifest_path.read_text(encoding="utf-8"))["documents"]}
    reviews = sorted(reviews_dir.glob("*.psv"))
    if not reviews:
        raise ValueError("authority review directory contains no PSV files")
    review_maps = []
    for review in reviews:
        lines = review.read_text(encoding="utf-8").splitlines()
        checksum = next((line.split("=", 1)[1] for line in lines if line.startswith("# corpusManifestSha256=")), "")
        if checksum != manifest_sha:
            raise ValueError(f"authority review does not match manifest: {review}")
        rows = list(csv.DictReader((line for line in lines if line and not line.startswith("#")), delimiter="|"))
        actual = {row["document_id"].strip() for row in rows}
        if actual != expected or len(rows) != len(actual):
            raise ValueError(f"authority review coverage mismatch: {review}")
        if any(row["review_status"].strip() not in {"REVIEWED", "APPROVED"} for row in rows):
            raise ValueError(f"authority review contains unreviewed rows: {review}")
        review_maps.append({row["document_id"].strip(): row["proposed_authority"].strip() for row in rows})
    consensus = {}
    for document_id in sorted(expected):
        values = {review[document_id] for review in review_maps}
        if len(values) != 1:
            raise ValueError(f"authority review disagreement: {document_id}")
        consensus[document_id] = values.pop()
    return consensus, reviews


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--now-epoch-millis", type=int, required=True)
    parser.add_argument("--trust-output", type=Path, required=True)
    parser.add_argument("--graph-output", type=Path, required=True)
    parser.add_argument("--authority-reviews-dir", type=Path)
    args = parser.parse_args()
    data = json.loads(args.manifest.read_text(encoding="utf-8"))
    reviewed_categories = None
    reviews = []
    if args.authority_reviews_dir:
        reviewed_categories, reviews = load_reviewed_categories(args.manifest, args.authority_reviews_dir)
    now = datetime.fromtimestamp(args.now_epoch_millis / 1000, timezone.utc)
    trust, graph = ["source_path|authority|days_ago|authority_category|verification_status\n"], ["source_path|target_paths\n"]
    for document in sorted(data["documents"], key=lambda item: item["text_path"]):
        category = reviewed_categories.get(document["document_id"], document["authority_category"]) if reviewed_categories else document["authority_category"]
        if category not in AUTHORITY:
            raise ValueError("no deterministic authority mapping for " + category)
        updated = datetime.fromisoformat(document["last_updated"].replace("Z", "+00:00")).astimezone(timezone.utc)
        days_ago = max(0, int((now - updated).total_seconds() // 86400))
        trust.append(f"{document['text_path']}|{AUTHORITY[category]:.2f}|{days_ago}|{category}|verified\n")
        graph.append(f"{document['text_path']}|{';'.join(document.get('graph_links', []))}\n")
    provenance = []
    if reviews:
        provenance = [f"# authorityReviewCount={len(reviews)}\n"] + [f"# authorityReviewSha256={sha256(review)}\n" for review in reviews]
    args.trust_output.write_text("".join(provenance + trust), encoding="utf-8", newline="\n")
    args.graph_output.write_text("".join(graph), encoding="utf-8", newline="\n")
    print(json.dumps({"status": "created", "documents": len(data["documents"]), "graph_edges": sum(bool(x.get("graph_links")) for x in data["documents"])}))


if __name__ == "__main__":
    main()
