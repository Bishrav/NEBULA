#!/usr/bin/env python3
"""Audit corpus licensing evidence without making a legal-clearance decision."""

import argparse
import json
import re
from collections import Counter
from pathlib import Path

REGISTRY_FIELDS = [
    "source_id", "project", "upstream_repository", "scope", "source_url",
    "license", "attribution", "license_evidence_url", "status", "notes",
]


def parse_registry(path):
    rows = []
    for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        fields = [item.strip() for item in line.split("|")]
        if fields == REGISTRY_FIELDS:
            continue
        if len(fields) != len(REGISTRY_FIELDS):
            raise ValueError(f"registry line {line_number}: expected {len(REGISTRY_FIELDS)} fields")
        rows.append(dict(zip(REGISTRY_FIELDS, fields)))
    return rows


def audit(registry_path, manifest_path, corpus_root):
    rows = parse_registry(registry_path)
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    documents = manifest.get("documents", [])
    license_root = corpus_root / "licenses"
    projects = Counter(doc.get("project") for doc in documents)
    findings = []
    structural_errors = []

    for row in rows:
        project = row["project"]
        source_id = row["source_id"]
        document_count = projects.get(project, 0)
        notice_candidates = [
            path for path in license_root.rglob("*")
            if path.is_file() and source_id.lower() in path.name.lower()
        ] if license_root.is_dir() else []

        row_errors = []
        if not re.fullmatch(r"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+", row["upstream_repository"]):
            row_errors.append("upstream_repository must be owner/repository")
        for field in ("source_url", "license_evidence_url"):
            if not row[field].startswith("https://"):
                row_errors.append(f"{field} must use https")
        if not row["license"]:
            row_errors.append("license is empty")
        if not row["attribution"]:
            row_errors.append("attribution is empty")
        if row["status"] != "ACCEPTED":
            row_errors.append(f"registry status is {row['status']}")
        if document_count == 0:
            row_errors.append("no manifest documents found for project")
        if not notice_candidates:
            row_errors.append("retained upstream notice file not found under licenses/")

        if row_errors:
            structural_errors.extend(f"{source_id}: {error}" for error in row_errors)

        findings.append({
            "source_id": source_id,
            "project": project,
            "document_count": document_count,
            "declared_license": row["license"],
            "attribution": row["attribution"],
            "license_evidence_url": row["license_evidence_url"],
            "retained_notice_files": [path.relative_to(corpus_root).as_posix() for path in notice_candidates],
            "issues": row_errors,
        })

    recorded_status = manifest.get("licensing_status", "NEEDS_HUMAN_LICENSING_REVIEW")
    return {
        "schema_version": "corpus-license-audit-v1",
        "corpus_version": manifest.get("corpus_version"),
        "structural_status": "PASS" if not structural_errors else "REVIEW_REQUIRED",
        "legal_clearance": recorded_status if recorded_status != "NEEDS_HUMAN_LICENSING_REVIEW" else "NOT_DECIDED",
        "human_review_required": recorded_status == "NEEDS_HUMAN_LICENSING_REVIEW",
        "document_count": len(documents),
        "source_count": len(rows),
        "structural_errors": structural_errors,
        "sources": findings,
        "review_scope": [
            "verify the upstream license text and version at the pinned commit",
            "retain applicable LICENSE, COPYRIGHT, NOTICE, or attribution files",
            "check third-party and generated content within the admitted paths",
            "confirm redistribution and attribution obligations for normalized derivatives",
        ],
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--registry", type=Path, required=True)
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--corpus-root", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    report = audit(args.registry, args.manifest, args.corpus_root)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(json.dumps({
        "status": report["structural_status"],
        "legal_clearance": report["legal_clearance"],
        "human_review_required": report["human_review_required"],
        "sources": report["source_count"],
        "documents": report["document_count"],
    }, sort_keys=True))
    return 0 if report["structural_status"] == "PASS" else 1


if __name__ == "__main__":
    raise SystemExit(main())
