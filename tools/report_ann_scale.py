"""Convert AnnScaleBenchmark JSON into flat CSV and Markdown reports."""

import argparse
import csv
import json
from pathlib import Path


FIELDS = [
    "vectors", "dimension", "M", "efConstruction", "efSearch", "repeat",
    "recallAt1", "recallAt5", "recallAt10", "recallAtK",
    "buildMillis", "estimatedIndexBytes", "heapDeltaBytes",
    "exactP50Millis", "exactP95Millis", "exactP99Millis",
    "hnswP50Millis", "hnswP95Millis", "hnswP99Millis",
]


def flatten(payload):
    rows = []
    for result in payload.get("results", []):
        for trial in result.get("trials", []):
            row = {
                "vectors": result["vectors"],
                "dimension": payload["dimension"],
                "M": result["M"],
                "efConstruction": result["efConstruction"],
                "efSearch": result["efSearch"],
            }
            row.update({field: trial.get(field) for field in FIELDS if field not in row})
            rows.append(row)
    return rows


def write_reports(payload, output_dir):
    output_dir.mkdir(parents=True, exist_ok=True)
    rows = flatten(payload)
    csv_path = output_dir / "ann-scale-trials.csv"
    with csv_path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=FIELDS)
        writer.writeheader()
        writer.writerows(rows)

    markdown_path = output_dir / "ann-scale-trials.md"
    with markdown_path.open("w", encoding="utf-8") as handle:
        handle.write("# ANN scale benchmark\n\n")
        handle.write("Synthetic-vector systems benchmark; not semantic retrieval evidence.\n\n")
        handle.write("| " + " | ".join(FIELDS) + " |\n")
        handle.write("| " + " | ".join("---" for _ in FIELDS) + " |\n")
        for row in rows:
            handle.write("| " + " | ".join(str(row.get(field, "")) for field in FIELDS) + " |\n")
    return csv_path, markdown_path


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("input", type=Path)
    parser.add_argument("--output-dir", type=Path, required=True)
    args = parser.parse_args()
    payload = json.loads(args.input.read_text(encoding="utf-8"))
    if payload.get("schemaVersion") != "ann-scale-v1":
        raise ValueError("unsupported ANN benchmark schema")
    csv_path, markdown_path = write_reports(payload, args.output_dir)
    print(f"GENERATED: {csv_path}")
    print(f"GENERATED: {markdown_path}")


if __name__ == "__main__":
    main()
