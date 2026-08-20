#!/usr/bin/env python3
"""Generate a reproducible Markdown PMF report from a validated NEBULA export."""

import argparse
import csv
import json
import statistics
import sys
from collections import Counter, defaultdict
from datetime import datetime, timezone
from pathlib import Path

from validate_research_export import validate


def as_bool(value):
    return value.strip().lower() == "true"


def percentile(values, fraction):
    if not values:
        return None
    ordered = sorted(values)
    return ordered[min(len(ordered) - 1, int((len(ordered) - 1) * fraction))]


def percent(numerator, denominator):
    return "n/a" if denominator == 0 else f"{numerator / denominator:.1%}"


def load_rows(path):
    with path.open(newline="", encoding="utf-8-sig") as handle:
        return list(csv.DictReader(handle))


def load_manifest(path):
    if path is None:
        return {}
    with path.open(encoding="utf-8") as handle:
        value = json.load(handle)
    if not isinstance(value, dict):
        raise ValueError("manifest must contain a JSON object")
    return value


def build_report(input_path, manifest_path=None, generated_at=None):
    validation = validate(input_path)
    if validation["status"] != "valid":
        details = "\n".join(f"- {message}" for message in validation["errors"])
        raise ValueError("research export failed validation:\n" + details)

    rows = load_rows(input_path)
    manifest = load_manifest(manifest_path)
    searches = [row for row in rows if row["type"] == "search"]
    feedback = [row for row in rows if row["type"] == "feedback"]
    tasks = [row for row in rows if row["type"] == "task"]
    observations = [row for row in rows if row["type"] == "observation"]
    completions = [row for row in tasks if row["action"] == "complete"]
    useful_feedback = [row for row in feedback if as_bool(row["useful"])]
    successful_tasks = [row for row in completions if as_bool(row["success"])]
    zero_result_searches = [row for row in searches if int(row["results"]) == 0]
    latency_ms = [int(row["latencyNanos"]) / 1_000_000 for row in searches]
    durations = [int(row["durationMs"]) for row in completions]
    confidence = [int(row["confidence"]) for row in observations]

    searches_by_task = Counter(row["taskId"] for row in searches if row["taskId"])
    task_rows = defaultdict(list)
    for row in completions:
        task_rows[row["taskId"]].append(row)
    task_lines = []
    for task_id in sorted(task_rows):
        task_completions = task_rows[task_id]
        successes = sum(1 for row in task_completions if as_bool(row["success"]))
        duration_values = [int(row["durationMs"]) for row in task_completions]
        task_lines.append(
            f"| `{task_id}` | {searches_by_task.get(task_id, 0)} | {len(task_completions)} "
            f"| {percent(successes, len(task_completions))} | {statistics.median(duration_values):,.0f} ms |"
        )

    timestamp = generated_at or datetime.now(timezone.utc).replace(microsecond=0).isoformat()
    synthetic = input_path.name.endswith(".sample.csv")
    source_label = "synthetic sample fixture" if synthetic else "pilot export"
    study_version = manifest.get("studyVersion", "not supplied")
    corpus_version = manifest.get("corpusVersion", "not supplied")
    study_wave = manifest.get("studyWave", "not supplied")
    query_set_version = manifest.get("querySetVersion", "not supplied")
    code_version = manifest.get("codeVersion", "not supplied")

    summary = [
        f"- **Evidence status.** This report uses a {source_label} with {len(rows)} valid events across {validation['sessions']} sessions.",
        f"- **Search usefulness.** Participants marked {len(useful_feedback)} of {len(feedback)} judged results useful ({percent(len(useful_feedback), len(feedback))}); {len(zero_result_searches)} of {len(searches)} searches returned no results.",
        f"- **Task outcome.** {len(successful_tasks)} of {len(completions)} completed tasks succeeded ({percent(len(successful_tasks), len(completions))}); median completion time was {statistics.median(durations):,.0f} ms." if durations else "- **Task outcome.** No completed tasks are available.",
        f"- **Confidence signal.** Average confidence was {statistics.mean(confidence):.2f}/5 across {len(confidence)} observations." if confidence else "- **Confidence signal.** No confidence observations are available.",
    ]
    if synthetic:
        summary.append("- **Interpretation boundary.** These numbers are a pipeline demonstration only and must not be presented as participant evidence.")

    notes = [row["note"].strip() for row in observations if row["note"].strip()]
    report = [
        "# NEBULA PMF Research Report",
        "",
        f"Generated: `{timestamp}`",
        "",
        "## Executive Summary",
        "",
        *summary,
        "",
        "## What the pilot measured",
        "",
        "This report evaluates whether engineering users can find and trust useful evidence with NEBULA. Search usefulness is the share of judged results marked useful; task success is the share of completed protocol tasks marked successful; confidence is a self-reported 1–5 rating. These are discovery metrics, not causal estimates or population-level product-market-fit proof.",
        "",
        "## Search behavior shows the main retrieval signal",
        "",
        f"The export contains {len(searches)} searches with median server latency of {statistics.median(latency_ms):,.1f} ms and a {percent(len(zero_result_searches), len(searches))} zero-result rate. The zero-result rate identifies where retrieval or query phrasing needs investigation; it does not prove that the corpus lacks an answer." if latency_ms else "No searches were recorded, so retrieval latency and zero-result rate are unavailable. The study should not interpret an empty export as evidence of search quality.",
        "",
        "| Metric | Value |",
        "| --- | ---: |",
        f"| Searches | {len(searches)} |",
        f"| Median latency | {statistics.median(latency_ms):,.1f} ms |" if latency_ms else "| Median latency | n/a |",
        f"| Zero-result searches | {len(zero_result_searches)} ({percent(len(zero_result_searches), len(searches))}) |",
        f"| Useful feedback | {len(useful_feedback)} / {len(feedback)} ({percent(len(useful_feedback), len(feedback))}) |",
        "",
        "## Task outcomes identify where the experience breaks down",
        "",
        "Task-level results connect user behavior to the protocol rather than treating every search as an independent success. Compare tasks only within the same study wave and protocol version.",
        "",
        "| Task | Searches | Completions | Success rate | Median duration |",
        "| --- | ---: | ---: | ---: | ---: |",
        *(task_lines or ["| No completed tasks | 0 | 0 | n/a | n/a |"]),
        "",
        "## Confidence and qualitative signal",
        "",
        f"Confidence observations average {statistics.mean(confidence):.2f}/5 with a median of {statistics.median(confidence):.1f}/5." if confidence else "No confidence observations were recorded.",
        "",
        "Qualitative notes captured in this export:" if notes else "No qualitative notes were captured.",
        *(f"- {note}" for note in notes),
        "",
        "## Recommended next steps",
        "",
        "1. Run the same protocol with a larger, pre-specified participant sample and a fixed corpus/query-set version.",
        "2. Investigate zero-result and low-confidence tasks using the evidence preview and reformulation analysis before changing ranking behavior.",
        "3. Preserve the export, manifest, validator output, notebook commit, and task sheet together as the study-wave record.",
        "",
        "## Further questions",
        "",
        "- Do users find the evidence useful without facilitator assistance?",
        "- Does trust-aware or hybrid ranking improve usefulness without increasing latency or reformulations?",
        "- Which task failures are caused by corpus coverage, query phrasing, ranking, or evidence presentation?",
        "",
        "## Caveats and provenance",
        "",
        f"- Source: `{input_path}`; validator status: `valid`; event counts: `{json.dumps(validation['eventCounts'], sort_keys=True)}`.",
        f"- Study version: `{study_version}`; corpus version: `{corpus_version}`; study wave: `{study_wave}`; query set: `{query_set_version}`; code version: `{code_version}`.",
        "- The sample fixture is synthetic. Real exports require consent, recruitment documentation, redaction review, and the limitations in `docs/research/pilot-protocol.md`.",
        "- Small samples, selection effects, task familiarity, and facilitator influence can materially change these metrics. Do not use this report to claim general product-market fit or causality.",
    ]
    return "\n".join(report).rstrip() + "\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("csv_file", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--manifest", type=Path, help="optional saved /v1/research/manifest JSON")
    parser.add_argument("--generated-at", help="fixed ISO timestamp for reproducible output")
    args = parser.parse_args()
    try:
        report = build_report(args.csv_file, args.manifest, args.generated_at)
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(report, encoding="utf-8")
    print(f"WROTE: {args.output}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
