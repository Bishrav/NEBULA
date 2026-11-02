#!/usr/bin/env python3
"""Generate dependency-free publication tables, plot data, and SVG figures."""

import argparse
import csv
import json
from pathlib import Path


def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def write_csv(path, headers, rows):
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(headers)
        writer.writerows(rows)


def escape(value):
    return (str(value).replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace('"', "&quot;"))


def bar_chart(variants, output):
    width, height, left, bottom = 900, 480, 70, 390
    plot_width, plot_height = width - left - 30, 320
    max_value = max([row["ndcgAtK"] for row in variants] + [1.0])
    bar_width = plot_width / max(len(variants), 1) * 0.72
    gap = plot_width / max(len(variants), 1)
    bars = []
    for index, row in enumerate(variants):
        value = row["ndcgAtK"]
        x = left + index * gap + (gap - bar_width) / 2
        bar_height = value / max_value * plot_height
        y = bottom - bar_height
        label = escape(row["name"])
        bars.append(f'<rect x="{x:.2f}" y="{y:.2f}" width="{bar_width:.2f}" height="{bar_height:.2f}" fill="#38bdf8"/>')
        bars.append(f'<text x="{x + bar_width / 2:.2f}" y="{bottom + 22}" text-anchor="middle" font-size="11" transform="rotate(25 {x + bar_width / 2:.2f} {bottom + 22})">{label}</text>')
        bars.append(f'<text x="{x + bar_width / 2:.2f}" y="{y - 6:.2f}" text-anchor="middle" font-size="11">{value:.3f}</text>')
    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">
<title>Regression fixture NDCG comparison</title><rect width="100%" height="100%" fill="#07111f"/>
<text x="{left}" y="35" fill="#e2e8f0" font-size="20">NDCG@k by ranking variant</text>
<text x="{left}" y="58" fill="#94a3b8" font-size="12">Descriptive regression fixture; not publication evidence</text>
<line x1="{left}" y1="{bottom}" x2="{width - 30}" y2="{bottom}" stroke="#64748b"/>
{''.join(bars)}<text x="18" y="{bottom - plot_height / 2}" fill="#cbd5e1" font-size="12" transform="rotate(-90 18 {bottom - plot_height / 2})">NDCG@k</text></svg>\n'''
    output.write_text(svg, encoding="utf-8")



def uncertainty_chart(results, output):
    width, height, left, right, top, bottom = 1000, 560, 270, 40, 55, 45
    rows = sorted(results, key=lambda row: row["meanDifference"], reverse=True)
    values = [value for row in rows for value in row["bootstrap95PercentCI"]] + [0.0]
    low, high = min(values), max(values)
    padding = max((high - low) * 0.08, 0.01)
    low, high = low - padding, high + padding
    plot_width = width - left - right
    row_height = (height - top - bottom) / max(len(rows), 1)

    def x(value):
        return left + (value - low) / (high - low) * plot_width

    marks = [f'<line x1="{x(0):.2f}" y1="{top - 12}" x2="{x(0):.2f}" y2="{height - bottom + 8}" stroke="#64748b" stroke-dasharray="4 4"/>']
    for index, row in enumerate(rows):
        y = top + index * row_height + row_height / 2
        ci_low, ci_high = row["bootstrap95PercentCI"]
        estimate = row["meanDifference"]
        label = escape(row["variant"])
        marks.append(f'<text x="{left - 12}" y="{y + 4:.2f}" text-anchor="end" fill="#334155" font-size="12">{label}</text>')
        marks.append(f'<line x1="{x(ci_low):.2f}" y1="{y:.2f}" x2="{x(ci_high):.2f}" y2="{y:.2f}" stroke="#2563eb" stroke-width="2"/>')
        marks.append(f'<line x1="{x(ci_low):.2f}" y1="{y - 5:.2f}" x2="{x(ci_low):.2f}" y2="{y + 5:.2f}" stroke="#2563eb"/>')
        marks.append(f'<line x1="{x(ci_high):.2f}" y1="{y - 5:.2f}" x2="{x(ci_high):.2f}" y2="{y + 5:.2f}" stroke="#2563eb"/>')
        marks.append(f'<circle cx="{x(estimate):.2f}" cy="{y:.2f}" r="5" fill="#0f766e"><title>{label}: {estimate:.4f} [{ci_low:.4f}, {ci_high:.4f}]</title></circle>')
    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">
<title>Paired NDCG differences versus BM25</title><desc>Mean paired NDCG difference with percentile bootstrap 95 percent confidence intervals.</desc>
<rect width="100%" height="100%" fill="#ffffff"/><text x="{left}" y="28" fill="#0f172a" font-size="20">Paired NDCG difference versus BM25</text>
<text x="{left}" y="46" fill="#64748b" font-size="12">Mean difference with percentile bootstrap 95% interval; regression fixture only</text>
<line x1="{left}" y1="{height - bottom}" x2="{width - right}" y2="{height - bottom}" stroke="#94a3b8"/>{''.join(marks)}
<text x="{(left + width - right) / 2:.2f}" y="{height - 10}" text-anchor="middle" fill="#334155" font-size="12">NDCG@k difference relative to BM25</text></svg>
'''
    output.write_text(svg, encoding="utf-8")


def paired_table(results, csv_output, latex_output):
    headers = ["variant", "metric", "n", "mean_difference", "median_difference", "ci_low", "ci_high", "win_rate", "permutation_p_value", "holm_adjusted_p_value"]
    rows = [[row["variant"], row["metric"], row["n"], row["meanDifference"], row["medianDifference"], row["bootstrap95PercentCI"][0], row["bootstrap95PercentCI"][1], row["winRate"], row["permutationPValue"], row["holmAdjustedPValue"]] for row in results]
    write_csv(csv_output, headers, rows)
    lines = ["% Generated by tools/generate_publication_package.py", "\\begin{tabular}{lrrrrrr}", "\\toprule", "Variant & Mean $\\Delta$ & Median $\\Delta$ & 95\\% CI low & 95\\% CI high & Win rate & Holm $p$ \\\\", "\\midrule"]
    for row in results:
        name = row["variant"].replace("_", "\\_")
        low, high = row["bootstrap95PercentCI"]
        lines.append(f"{name} & {row['meanDifference']:.4f} & {row['medianDifference']:.4f} & {low:.4f} & {high:.4f} & {row['winRate']:.3f} & {row['holmAdjustedPValue']:.4f} \\\\")
    lines.extend(["\\bottomrule", "\\end{tabular}", ""])
    latex_output.write_text("\n".join(lines), encoding="utf-8")

def latex_table(variants, output):
    lines = ["% Generated by tools/generate_publication_package.py", "\\begin{tabular}{lrrrr}", "\\toprule", "Variant & Precision@k & Recall@k & MRR & NDCG@k \\\\", "\\midrule"]
    for row in variants:
        name = row["name"].replace("_", "\\_")
        lines.append(f"{name} & {row['precisionAtK']:.4f} & {row['recallAtK']:.4f} & {row['mrr']:.4f} & {row['ndcgAtK']:.4f} \\\\")
    lines.extend(["\\bottomrule", "\\end{tabular}", ""])
    output.write_text("\n".join(lines), encoding="utf-8")


def generate(benchmark, ann, distributed, output_dir, paired=None):
    output_dir.mkdir(parents=True, exist_ok=True)
    variants = benchmark["variants"]
    write_csv(output_dir / "ranking-comparison.csv",
              ["variant", "precision_at_k", "recall_at_k", "mrr", "ndcg_at_k", "delta_ndcg_vs_bm25"],
              [[row["name"], row["precisionAtK"], row["recallAtK"], row["mrr"], row["ndcgAtK"], row["deltaNdcgVsBm25"]] for row in variants])
    latex_table(variants, output_dir / "ranking-comparison.tex")
    (output_dir / "ranking-plot-data.json").write_text(json.dumps({
        "schemaVersion": "publication-plot-data-v1", "evidenceClass": "REGRESSION_FIXTURE_ONLY",
        "metric": "ndcgAtK", "cutoff": benchmark["cutoff"], "variants": variants
    }, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    bar_chart(variants, output_dir / "ranking-ndcg.svg")
    if paired:
        paired_table(paired["results"], output_dir / "paired-ndcg-comparison.csv", output_dir / "paired-ndcg-comparison.tex")
        uncertainty_chart(paired["results"], output_dir / "paired-ndcg-differences.svg")
    write_csv(output_dir / "ann-comparison.csv", ["method", "recall_at_k", "mean_latency_ms"], [
        ["exact_cosine", ann["exactRecallAtK"], ann["exactMeanLatencyMs"]],
        ["hnsw", ann["hnswRecallAtK"], ann["hnswMeanLatencyMs"]]])
    distributed_rows = [[row["scenario"], row.get("status"), row.get("throughputPerSecond"), row.get("p50LatencyMillis"), row.get("p95LatencyMillis"), row.get("partialResultFrequency"), row.get("ndcgDegradation")] for row in distributed]
    if distributed_rows:
        write_csv(output_dir / "distributed-summary.csv", ["scenario", "status", "throughput_per_second", "p50_ms", "p95_ms", "partial_result_frequency", "ndcg_degradation"], distributed_rows)
    (output_dir / "README.md").write_text("""# Publication artifact bundle\n\nGenerated tables, LaTeX, plot data, and a dependency-free SVG figure.\n\n**Evidence status:** `REGRESSION_FIXTURE_ONLY` unless an artifact manifest explicitly says otherwise. These outputs must not be used to claim ranking superiority, statistical significance, user benefit, or production performance.\n\nRe-run after the corpus, qrels, configuration, and model are frozen. Do not manually copy values into a paper.\n""", encoding="utf-8")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--benchmark", type=Path, required=True)
    parser.add_argument("--ann", type=Path, required=True)
    parser.add_argument("--distributed", type=Path, nargs="*", default=[])
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--paired", type=Path)
    args = parser.parse_args()
    generate(load(args.benchmark), load(args.ann), [load(path) for path in args.distributed], args.output_dir, load(args.paired) if args.paired else None)
    print(f"GENERATED: {args.output_dir}")


if __name__ == "__main__":
    main()
