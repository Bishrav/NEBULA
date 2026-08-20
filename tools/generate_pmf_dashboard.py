#!/usr/bin/env python3
"""Generate a self-contained, read-only PMF comparison dashboard."""

import argparse
import csv
import html
import json
import statistics
import sys
from collections import defaultdict
from pathlib import Path

from generate_pmf_report import load_manifest
from validate_research_export import validate


def load_rows(path):
    with path.open(newline="", encoding="utf-8-sig") as handle:
        return list(csv.DictReader(handle))


def comparison_rows(rows):
    searches = defaultdict(list)
    feedback = defaultdict(list)
    for row in rows:
        if row["type"] == "search":
            searches[row["mode"]].append(row)
        elif row["type"] == "feedback":
            feedback[row["mode"]].append(row)
    result = []
    for mode in sorted(set(searches) | set(feedback)):
        search_rows = searches[mode]
        feedback_rows = feedback[mode]
        latencies = [int(row["latencyNanos"]) / 1_000_000 for row in search_rows]
        useful = sum(1 for row in feedback_rows if row["useful"].strip().lower() == "true")
        zero_results = sum(1 for row in search_rows if int(row["results"]) == 0)
        result.append({
            "mode": mode,
            "searches": len(search_rows),
            "feedback": len(feedback_rows),
            "usefulRate": None if not feedback_rows else useful / len(feedback_rows),
            "zeroResultRate": None if not search_rows else zero_results / len(search_rows),
            "medianLatencyMs": None if not latencies else statistics.median(latencies),
        })
    return result


def task_rows(rows):
    tasks = defaultdict(list)
    for row in rows:
        if row["type"] == "task" and row["action"] == "complete":
            tasks[row["taskId"]].append(row)
    result = []
    for task_id in sorted(tasks):
        values = tasks[task_id]
        successes = sum(1 for row in values if row["success"].strip().lower() == "true")
        durations = [int(row["durationMs"]) for row in values]
        result.append({
            "taskId": task_id,
            "completions": len(values),
            "successRate": successes / len(values),
            "medianDurationMs": statistics.median(durations),
        })
    return result


def build_dashboard(input_path, manifest_path=None):
    validation = validate(input_path)
    if validation["status"] != "valid":
        details = "\n".join(f"- {message}" for message in validation["errors"])
        raise ValueError("research export failed validation:\n" + details)
    rows = load_rows(input_path)
    manifest = load_manifest(manifest_path)
    searches = [row for row in rows if row["type"] == "search"]
    feedback = [row for row in rows if row["type"] == "feedback"]
    tasks = task_rows(rows)
    confidence = [int(row["confidence"]) for row in rows if row["type"] == "observation"]
    useful = sum(1 for row in feedback if row["useful"].strip().lower() == "true")
    latency = [int(row["latencyNanos"]) / 1_000_000 for row in searches]
    data = {
        "comparison": comparison_rows(rows),
        "tasks": tasks,
        "summary": {
            "events": len(rows),
            "sessions": validation["sessions"],
            "searches": len(searches),
            "usefulRate": None if not feedback else useful / len(feedback),
            "medianLatencyMs": None if not latency else statistics.median(latency),
            "confidence": None if not confidence else statistics.mean(confidence),
        },
        "provenance": {
            "source": str(input_path),
            "synthetic": input_path.name.endswith(".sample.csv"),
            "studyVersion": manifest.get("studyVersion", "not supplied"),
            "corpusVersion": manifest.get("corpusVersion", "not supplied"),
            "studyWave": manifest.get("studyWave", "not supplied"),
            "querySetVersion": manifest.get("querySetVersion", "not supplied"),
            "codeVersion": manifest.get("codeVersion", "not supplied"),
        },
    }
    payload = json.dumps(data, ensure_ascii=False, separators=(",", ":")).replace("<", "\\u003c")
    title = "NEBULA PMF Experiment Dashboard"
    provenance = data["provenance"]
    warning = "Synthetic fixture — do not use as participant evidence." if provenance["synthetic"] else "Pilot export — interpret within the approved study protocol."
    return f'''<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>{title}</title>
<style>
:root {{ color-scheme: light dark; --bg:#0b1020; --panel:#141b2d; --text:#f4f7fb; --muted:#aab5c7; --accent:#75d6c2; --line:#2b3854; }}
* {{ box-sizing:border-box; }} body {{ margin:0; font-family:Inter,ui-sans-serif,system-ui,sans-serif; background:var(--bg); color:var(--text); }}
main {{ max-width:1120px; margin:0 auto; padding:36px 20px 64px; }} h1 {{ margin:0 0 8px; font-size:clamp(28px,4vw,44px); }} h2 {{ margin:34px 0 12px; font-size:22px; }} p {{ color:var(--muted); line-height:1.55; }}
.banner {{ border:1px solid #9f7b2d; background:#2c2515; color:#f4d58a; border-radius:12px; padding:12px 16px; margin:22px 0; }}
.cards {{ display:grid; grid-template-columns:repeat(auto-fit,minmax(170px,1fr)); gap:12px; margin:24px 0; }} .card, .panel {{ background:var(--panel); border:1px solid var(--line); border-radius:14px; padding:18px; }} .label {{ color:var(--muted); font-size:13px; }} .value {{ font-size:28px; font-weight:700; margin-top:8px; color:var(--accent); }}
.controls {{ display:flex; align-items:center; gap:10px; margin:16px 0; }} select {{ background:var(--panel); color:var(--text); border:1px solid var(--line); border-radius:8px; padding:8px 10px; }}
table {{ width:100%; border-collapse:collapse; }} th,td {{ padding:11px 10px; border-bottom:1px solid var(--line); text-align:left; }} th {{ color:var(--muted); font-size:13px; }} td.num, th.num {{ text-align:right; }} .empty {{ color:var(--muted); padding:16px 0; }} .meta {{ font-size:13px; color:var(--muted); word-break:break-word; }}
@media (max-width:680px) {{ main {{ padding:24px 14px 48px; }} .panel {{ overflow-x:auto; }} table {{ min-width:650px; }} }}
</style></head>
<body><main>
<h1>{title}</h1><p>Read-only comparison surface for validated NEBULA research telemetry.</p>
<div class="banner">{warning}</div>
<section class="cards" id="summary"></section>
<section><h2>Ranking-mode comparison</h2><p>Compare search volume, judged usefulness, zero-result rate, and median server latency. Rates use the available event denominator; “n/a” means no observations were recorded.</p>
<div class="controls"><label for="mode">Focus mode:</label><select id="mode"><option value="all">All modes</option></select></div><div class="panel"><table><thead><tr><th>Mode</th><th class="num">Searches</th><th class="num">Useful rate</th><th class="num">Zero-result rate</th><th class="num">Median latency</th></tr></thead><tbody id="comparison"></tbody></table></div></section>
<section><h2>Protocol task outcomes</h2><p>Task metrics are completed-task outcomes, not independent search success rates.</p><div class="panel"><table><thead><tr><th>Task</th><th class="num">Completions</th><th class="num">Success rate</th><th class="num">Median duration</th></tr></thead><tbody id="tasks"></tbody></table></div></section>
<section><h2>Study provenance and limitations</h2><p class="meta">Source: {html.escape(provenance["source"])}<br>Study: {html.escape(str(provenance["studyVersion"]))} · Corpus: {html.escape(str(provenance["corpusVersion"]))} · Wave: {html.escape(str(provenance["studyWave"]))} · Query set: {html.escape(str(provenance["querySetVersion"]))} · Code: {html.escape(str(provenance["codeVersion"]))}</p><p>Small samples, selection effects, task familiarity, and facilitator influence can materially change these metrics. This dashboard supports product learning; it does not establish general product-market fit or causality.</p></section>
</main><script>const DATA={payload};
const esc=v=>String(v).replace(/[&<>"']/g,c=>c==='&'?'&amp;':c==='<'?'&lt;':c==='>'?'&gt;':c==='"'?'&quot;':'&#39;'); const fmtRate=v=>v===null?'n/a':(v*100).toFixed(1)+'%'; const fmtMs=v=>v===null?'n/a':v.toFixed(1)+' ms';
const summary=document.querySelector('#summary'); const s=DATA.summary; summary.innerHTML=[['Events',s.events],['Sessions',s.sessions],['Searches',s.searches],['Useful feedback',fmtRate(s.usefulRate)],['Median latency',fmtMs(s.medianLatencyMs)],['Confidence',s.confidence===null?'n/a':s.confidence.toFixed(2)+'/5']].map(x=>'<div class="card"><div class="label">'+x[0]+'</div><div class="value">'+x[1]+'</div></div>').join('');
const mode=document.querySelector('#mode'); DATA.comparison.forEach(x=>mode.insertAdjacentHTML('beforeend',`<option value="${{esc(x.mode)}}">${{esc(x.mode)}}</option>`));
function render() {{ const chosen=mode.value; const rows=DATA.comparison.filter(x=>chosen==='all'||x.mode===chosen); document.querySelector('#comparison').innerHTML=rows.length?rows.map(x=>`<tr><td>${{esc(x.mode)}}</td><td class="num">${{x.searches}}</td><td class="num">${{fmtRate(x.usefulRate)}}</td><td class="num">${{fmtRate(x.zeroResultRate)}}</td><td class="num">${{fmtMs(x.medianLatencyMs)}}</td></tr>`).join(''):'<tr><td colspan="5" class="empty">No comparison data.</td></tr>'; }}
document.querySelector('#tasks').innerHTML=DATA.tasks.length?DATA.tasks.map(x=>`<tr><td>${{esc(x.taskId)}}</td><td class="num">${{x.completions}}</td><td class="num">${{fmtRate(x.successRate)}}</td><td class="num">${{x.medianDurationMs.toFixed(0)}} ms</td></tr>`).join(''):'<tr><td colspan="4" class="empty">No completed tasks.</td></tr>'; mode.addEventListener('change',render); render();</script></body></html>'''


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("csv_file", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--manifest", type=Path)
    args = parser.parse_args()
    try:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(build_dashboard(args.csv_file, args.manifest), encoding="utf-8")
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1
    print(f"WROTE: {args.output}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
