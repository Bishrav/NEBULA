#!/usr/bin/env python3
"""Generate auditable corpus statistics and licensing/attribution summaries."""

import argparse
import json
from collections import defaultdict
from pathlib import Path


def build(manifest, rejected):
    groups = defaultdict(lambda: {"documents": 0, "words": 0})
    for doc in manifest["documents"]:
        key = (doc["project"], doc["source_type"], doc["version"], doc["license"])
        groups[key]["documents"] += 1
        groups[key]["words"] += doc.get("word_count", 0)
    composition = [{"project": p, "source_type": s, "version": v, "license": l,
                    "documents": value["documents"], "words": value["words"]}
                   for (p, s, v, l), value in sorted(groups.items())]
    return {
        "schema_version": "research-corpus-statistics-v1",
        "corpus_version": manifest["corpus_version"],
        "provisional": manifest.get("provisional", False),
        "licensing_status": manifest.get("licensing_status"),
        "documents": len(manifest["documents"]),
        "words": sum(doc.get("word_count", 0) for doc in manifest["documents"]),
        "rejected": len(rejected),
        "composition": composition,
        "licenses": sorted({doc["license"] for doc in manifest["documents"]}),
        "attributions": sorted({doc["attribution"] for doc in manifest["documents"]}),
    }


def markdown(stats, rejected):
    lines = ["# research-corpus-v1 report", "", f"**Status:** `{'PROVISIONAL — NEEDS HUMAN LICENSING REVIEW' if stats['provisional'] else 'ADMITTED'}`", "",
             f"Documents: **{stats['documents']}**  ", f"Words: **{stats['words']}**  ", f"Rejected during acquisition: **{stats['rejected']}**", "",
             "## Composition", "", "| Project | Source type | Version | License | Documents | Words |", "| --- | --- | --- | --- | ---: | ---: |"]
    for row in stats["composition"]:
        lines.append(f"| {row['project']} | {row['source_type']} | {row['version']} | {row['license']} | {row['documents']} | {row['words']} |")
    lines += ["", "## License and attribution", "", "The snapshot contains only source-level license declarations recorded in the manifest. This is not a legal clearance. Human review must verify third-party notices, generated material, attribution, and redistribution obligations before the corpus is treated as a final public release.", ""]
    for license_name in stats["licenses"]:
        lines.append(f"- **{license_name}**")
    lines += ["", "Attributions:"]
    for attribution in stats["attributions"]:
        lines.append(f"- {attribution}")
    lines += ["", "## Rejected-source report", ""]
    if not rejected:
        lines.append("No files were rejected during acquisition.")
    else:
        lines += ["| Source | Path | Reason |", "| --- | --- | --- |"]
        for item in rejected:
            lines.append(f"| {item.get('source_id', '')} | {item.get('path', '')} | {item.get('reason', '')} |")
    return "\n".join(lines) + "\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--rejected", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    args = parser.parse_args()
    manifest = json.loads(args.manifest.read_text(encoding="utf-8"))
    rejected = json.loads(args.rejected.read_text(encoding="utf-8"))
    stats = build(manifest, rejected)
    args.output_dir.mkdir(parents=True, exist_ok=True)
    (args.output_dir / "corpus-statistics.json").write_text(json.dumps(stats, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    (args.output_dir / "corpus-report.md").write_text(markdown(stats, rejected), encoding="utf-8")
    (args.output_dir / "license-attribution-summary.json").write_text(json.dumps({"licenses": stats["licenses"], "attributions": stats["attributions"], "status": "NEEDS_HUMAN_LICENSING_REVIEW"}, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(json.dumps({"status": "created", "documents": stats["documents"], "words": stats["words"], "rejected": stats["rejected"]}, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
