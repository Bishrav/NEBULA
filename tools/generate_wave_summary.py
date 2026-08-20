#!/usr/bin/env python3
"""Generate session-completeness and outcome summaries for a NEBULA study wave."""

import argparse
import csv
import json
import statistics
import sys
from collections import Counter, defaultdict
from pathlib import Path

from validate_research_export import validate


def summarize(input_path, required_tasks):
    validation = validate(input_path)
    if validation["status"] != "valid":
        raise ValueError("wave export failed validation")
    with input_path.open(newline="", encoding="utf-8-sig") as handle:
        rows = list(csv.DictReader(handle))
    by_session = defaultdict(list)
    for row in rows:
        by_session[row["sessionId"]].append(row)
    sessions = []
    for session_id in sorted(by_session):
        session_rows = by_session[session_id]
        tasks = {row["taskId"]: row for row in session_rows if row["type"] == "task" and row["action"] == "complete"}
        searches = [row for row in session_rows if row["type"] == "search"]
        feedback = [row for row in session_rows if row["type"] == "feedback"]
        observations = [row for row in session_rows if row["type"] == "observation"]
        durations = [int(row["durationMs"]) for row in tasks.values()]
        successful = sum(1 for row in tasks.values() if row["success"].lower() == "true")
        useful = sum(1 for row in feedback if row["useful"].lower() == "true")
        missing_tasks = sorted(set(required_tasks) - set(tasks))
        sessions.append({
            "sessionId": session_id,
            "events": len(session_rows),
            "eventTypes": dict(sorted(Counter(row["type"] for row in session_rows).items())),
            "searches": len(searches),
            "feedback": len(feedback),
            "usefulRate": None if not feedback else useful / len(feedback),
            "completedTasks": len(tasks),
            "successfulTasks": successful,
            "taskSuccessRate": None if not tasks else successful / len(tasks),
            "medianTaskDurationMs": None if not durations else statistics.median(durations),
            "observations": len(observations),
            "averageConfidence": None if not observations else statistics.mean(int(row["confidence"]) for row in observations),
            "missingTasks": missing_tasks,
            "complete": not missing_tasks,
        })
    return {
        "status": "valid",
        "source": str(input_path),
        "requiredTasks": required_tasks,
        "sessions": sessions,
        "sessionCount": len(sessions),
        "completeSessionCount": sum(1 for session in sessions if session["complete"]),
        "validation": validation,
    }


def markdown(summary):
    lines = [
        "# NEBULA Study-Wave Summary", "",
        f"Source: `{summary['source']}`", "",
        f"- Sessions: {summary['sessionCount']}",
        f"- Complete protocol sessions: {summary['completeSessionCount']} / {summary['sessionCount']}",
        f"- Required tasks: {', '.join(summary['requiredTasks'])}", "",
        "## Session completeness and outcomes", "",
        "| Session | Events | Searches | Feedback useful | Tasks | Success | Median duration | Confidence | Status |",
        "| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |",
    ]
    for session in summary["sessions"]:
        rate = "n/a" if session["usefulRate"] is None else f"{session['usefulRate']:.1%}"
        success = "n/a" if session["taskSuccessRate"] is None else f"{session['taskSuccessRate']:.1%}"
        duration = "n/a" if session["medianTaskDurationMs"] is None else f"{session['medianTaskDurationMs']:,.0f} ms"
        confidence = "n/a" if session["averageConfidence"] is None else f"{session['averageConfidence']:.2f}/5"
        status = "complete" if session["complete"] else "incomplete: " + ", ".join(session["missingTasks"])
        lines.append(f"| `{session['sessionId']}` | {session['events']} | {session['searches']} | {rate} | {session['completedTasks']} | {success} | {duration} | {confidence} | {status} |")
    lines += ["", "## Interpretation guardrails", "", "Session IDs are anonymous groupings, not verified participant identities. Incomplete sessions must remain in the data and be reported rather than silently excluded. This wave summary is descriptive and exploratory; it does not establish causality or population-level product-market fit.", ""]
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input_csv", type=Path)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--required-tasks", default="deployment-rollback,shard-failure,retrieval-explanation")
    args = parser.parse_args()
    required_tasks = [task.strip() for task in args.required_tasks.split(",") if task.strip()]
    try:
        result = summarize(args.input_csv, required_tasks)
        args.output_dir.mkdir(parents=True, exist_ok=True)
        (args.output_dir / "wave-summary.json").write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        (args.output_dir / "wave-summary.md").write_text(markdown(result), encoding="utf-8")
    except (OSError, ValueError, csv.Error) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1
    print(f"WROTE: {args.output_dir / 'wave-summary.md'}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
