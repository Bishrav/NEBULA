"""Create separate blinded annotator packages from a private retrieval artifact."""

import argparse
import csv
import hashlib
import json
import random
import zipfile
from collections import OrderedDict
from io import StringIO
from pathlib import Path


PACKET_FIELDS = [
    "annotation_query_id", "query_text", "document_id", "document_title",
    "document_excerpt", "relevance_grade_0_3", "uncertainty",
    "evidence_note", "annotated_at",
]

RUBRIC = """# NEBULA blinded relevance annotation rubric

Annotate each query-document pair independently. Do not infer the system that
retrieved the document. Assign one grade:

- 0 — Irrelevant: does not help answer the query.
- 1 — Useful context: related background, but does not substantially answer it.
- 2 — Substantially relevant: addresses an important part of the information need.
- 3 — Directly answers: gives an actionable, sufficiently specific answer.

Set uncertainty when the grade is difficult to determine. Use the evidence note
to briefly record the passage or reason supporting the grade. Do not add
personal information. Complete the timestamp when you submit the judgment.
"""

ANNOTATOR_README = """# NEBULA annotation package

This package contains one blinded annotation assignment. It contains the same
query-document pairs as the other assignment, but candidate order was shuffled
independently. Do not compare packages or share judgments. Do not add ranking
scores, retrieval ranks, source URLs, grounding IDs, or system guesses.

Status: HUMAN ANNOTATION REQUIRED. These files contain no pre-filled labels.
"""


def load_candidates(path):
    with path.open(encoding="utf-8", newline="") as handle:
        rows = list(csv.DictReader(handle, delimiter="|"))
    required = {"query_id", "annotation_query_id", "document_id", "retrieval_rank", "title", "excerpt"}
    if not rows or not required.issubset(rows[0]):
        raise ValueError("retrieval candidate schema is incomplete")
    grouped = OrderedDict()
    for row in rows:
        grouped.setdefault((row["query_id"], row["annotation_query_id"]), []).append(row)
    if any(not values for values in grouped.values()):
        raise ValueError("query has no retrieved documents")
    return grouped


def load_queries(path):
    with path.open(encoding="utf-8", newline="") as handle:
        rows = list(csv.DictReader(handle, delimiter="|"))
    if not rows or set(rows[0]) != {"query_id", "query_text"}:
        raise ValueError("annotation query source schema must be query_id|query_text")
    return {row["query_id"]: row["query_text"] for row in rows}


def packet_text(rows, annotator, seed):
    by_query = OrderedDict()
    for row in rows:
        by_query.setdefault(row["annotation_query_id"], []).append(row)
    output = StringIO()
    writer = csv.DictWriter(output, fieldnames=PACKET_FIELDS, delimiter="|", lineterminator="\n")
    writer.writeheader()
    for annotation_id, candidates in by_query.items():
        randomizer = random.Random(int(hashlib.sha256(f"{seed}|{annotator}|{annotation_id}".encode()).hexdigest()[:16], 16))
        shuffled = list(candidates)
        randomizer.shuffle(shuffled)
        for candidate in shuffled:
            writer.writerow({
                "annotation_query_id": annotation_id,
                "query_text": candidate["query_text"],
                "document_id": candidate["document_id"],
                "document_title": candidate["title"],
                "document_excerpt": candidate["excerpt"],
                "relevance_grade_0_3": "",
                "uncertainty": "",
                "evidence_note": "",
                "annotated_at": "",
            })
    return output.getvalue()


def build_packages(candidates_path, queries_path, private_dir, output_dir, seed, packet_size):
    grouped = load_candidates(candidates_path)
    query_texts = load_queries(queries_path)
    for (query_id, _), values in grouped.items():
        if query_id not in query_texts:
            raise ValueError(f"retrieval query missing from frozen source: {query_id}")
        for row in values:
            row["query_text"] = query_texts[query_id]
    private_dir.mkdir(parents=True, exist_ok=True)
    output_dir.mkdir(parents=True, exist_ok=True)
    key_path = private_dir / "PRIVATE-query-id-key.psv"
    key_path.write_text("annotation_query_id|original_query_id\n" + "\n".join(
        f"{annotation_id}|{query_id}" for query_id, annotation_id in grouped
    ) + "\n", encoding="utf-8")
    private_copy = private_dir / "retrieval-candidates.psv"
    private_copy.write_bytes(candidates_path.read_bytes())
    packages = {}
    for annotator in ("A", "B"):
        annotator_dir = output_dir / f"annotator-{annotator}"
        annotator_dir.mkdir(parents=True, exist_ok=True)
        all_rows = [row for values in grouped.values() for row in values]
        text = packet_text(all_rows, annotator, seed)
        rows = list(csv.DictReader(StringIO(text), delimiter="|"))
        packet_paths = []
        for index in range(0, len(rows), packet_size):
            path = annotator_dir / f"annotation-packet-{index // packet_size + 1:02d}.psv"
            with path.open("w", encoding="utf-8", newline="") as handle:
                writer = csv.DictWriter(handle, fieldnames=PACKET_FIELDS, delimiter="|", lineterminator="\n")
                writer.writeheader()
                writer.writerows(rows[index:index + packet_size])
            packet_paths.append(path)
        zip_path = output_dir / f"blinded-annotation-package-{annotator}.zip"
        with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as archive:
            for packet in packet_paths:
                archive.write(packet, packet.relative_to(output_dir).as_posix())
            archive.writestr("ANNOTATION-RUBRIC.md", RUBRIC)
            archive.writestr("README.md", ANNOTATOR_README)
        packages[annotator] = {"rows": len(rows), "packets": len(packet_paths), "zip": str(zip_path)}
    manifest = {
        "schemaVersion": "blinded-annotation-packages-v2",
        "queryCount": len(grouped),
        "candidatePairCount": sum(len(rows) for rows in grouped.values()),
        "annotators": ["A", "B"],
        "seed": seed,
        "packetSize": packet_size,
        "privateMappingExcludedFromPackages": True,
        "retrievalScoresExcludedFromPackages": True,
        "rankingMetadataExcludedFromPackages": True,
        "packages": packages,
        "status": "HUMAN_ANNOTATION_REQUIRED",
    }
    (private_dir / "package-manifest.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    return manifest


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--candidates", type=Path, required=True)
    parser.add_argument("--queries", type=Path, required=True)
    parser.add_argument("--private-dir", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--seed", type=int, default=20260822)
    parser.add_argument("--packet-size", type=int, default=25)
    args = parser.parse_args()
    if args.packet_size <= 0:
        raise ValueError("packet size must be positive")
    print(json.dumps(build_packages(args.candidates, args.queries, args.private_dir, args.output_dir, args.seed, args.packet_size), indent=2))


if __name__ == "__main__":
    main()
