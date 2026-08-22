#!/usr/bin/env python3
"""Build qrels only from an explicitly release-ready adjudication artifact."""

import argparse
import json
import sys
from pathlib import Path


def build(release_path, adjudication_path, output):
    release = json.loads(release_path.read_text(encoding="utf-8"))
    if release.get("status") != "release-ready": raise ValueError("qrels build requires status=release-ready")
    packet = json.loads(adjudication_path.read_text(encoding="utf-8"))
    if not packet.get("items"): raise ValueError("adjudication packet contains no resolved items")
    lines = ["# Final qrels generated only after human annotation release gate", "query_id|document_id|relevance_grade"]
    for item in sorted(packet["items"], key=lambda value: (value["queryId"], value["sourcePath"])):
        grade = item.get("adjudicatedGrade")
        if grade not in range(4): raise ValueError("all adjudication items require grades 0..3")
        document_id = item.get("documentId", item.get("sourcePath"))
        if not document_id: raise ValueError("adjudication item is missing documentId/sourcePath")
        lines.append(f"{item['queryId']}|{document_id}|{grade}")
    output.parent.mkdir(parents=True, exist_ok=True); output.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return {"status": "created", "rows": len(lines) - 2, "humanAnnotationRequired": True}


def main():
    parser = argparse.ArgumentParser(description=__doc__); parser.add_argument("--release", type=Path, required=True); parser.add_argument("--adjudication", type=Path, required=True); parser.add_argument("--output", type=Path, required=True); args = parser.parse_args()
    try: print(json.dumps(build(args.release, args.adjudication, args.output), sort_keys=True)); return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc: print(f"ERROR: {exc}", file=sys.stderr); return 1


if __name__ == "__main__": raise SystemExit(main())
