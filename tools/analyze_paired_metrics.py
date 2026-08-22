#!/usr/bin/env python3
"""Compute paired bootstrap intervals, permutation p-values, and Holm correction."""

import argparse
import json
import random
import statistics
import sys
from pathlib import Path


def percentile(values, fraction):
    ordered = sorted(values)
    if not ordered:
        return 0.0
    position = (len(ordered) - 1) * fraction
    lower = int(position)
    upper = min(lower + 1, len(ordered) - 1)
    weight = position - lower
    return ordered[lower] * (1.0 - weight) + ordered[upper] * weight


def mean(values):
    return statistics.fmean(values) if values else 0.0


def paired_rows(data, variant, baseline, metric):
    variants = {row["name"]: row for row in data["variants"]}
    if variant not in variants or baseline not in variants:
        raise ValueError(f"missing variant or baseline: {variant}, {baseline}")
    left = {row["queryId"]: row[metric] for row in variants[variant].get("perQuery", [])}
    right = {row["queryId"]: row[metric] for row in variants[baseline].get("perQuery", [])}
    ids = sorted(set(left) & set(right))
    if len(ids) < 2:
        raise ValueError("at least two overlapping per-query observations are required")
    return [left[query_id] - right[query_id] for query_id in ids], ids


def bootstrap_mean(differences, resamples, rng):
    return [mean([differences[rng.randrange(len(differences))] for _ in differences]) for _ in range(resamples)]


def permutation_p_value(differences, permutations, rng):
    observed = abs(mean(differences))
    extreme = 0
    for _ in range(permutations):
        shuffled = [value if rng.random() < 0.5 else -value for value in differences]
        if abs(mean(shuffled)) >= observed:
            extreme += 1
    return (extreme + 1.0) / (permutations + 1.0)


def holm(p_values):
    ordered = sorted(p_values.items(), key=lambda item: item[1])
    adjusted = {}
    previous = 0.0
    count = len(ordered)
    for rank, (name, p_value) in enumerate(ordered):
        corrected = min(1.0, p_value * (count - rank))
        corrected = max(previous, corrected)
        adjusted[name] = corrected
        previous = corrected
    return adjusted


def analyze(data, baseline, metric, seed, resamples, permutations):
    rng = random.Random(seed)
    results = []
    raw_p = {}
    for variant in [row["name"] for row in data["variants"] if row["name"] != baseline]:
        differences, ids = paired_rows(data, variant, baseline, metric)
        samples = bootstrap_mean(differences, resamples, rng)
        p_value = permutation_p_value(differences, permutations, rng)
        raw_p[variant] = p_value
        results.append({
            "variant": variant,
            "baseline": baseline,
            "metric": metric,
            "n": len(differences),
            "queryIds": ids,
            "meanDifference": mean(differences),
            "medianDifference": statistics.median(differences),
            "winRate": sum(value > 0 for value in differences) / len(differences),
            "bootstrap95PercentCI": [percentile(samples, 0.025), percentile(samples, 0.975)],
            "permutationPValue": p_value,
        })
    corrected = holm(raw_p)
    for result in results:
        result["holmAdjustedPValue"] = corrected[result["variant"]]
    return {
        "schemaVersion": "paired-statistics-v1",
        "baseline": baseline,
        "metric": metric,
        "seed": seed,
        "bootstrapResamples": resamples,
        "permutationCount": permutations,
        "results": results,
        "interpretation": "Descriptive paired uncertainty; do not claim significance when the sample or design is insufficient.",
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--comparison", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--baseline", default="bm25")
    parser.add_argument("--metric", choices=("precisionAtK", "recallAtK", "mrr", "ndcgAtK"), default="ndcgAtK")
    parser.add_argument("--seed", type=int, default=20260822)
    parser.add_argument("--bootstrap-resamples", type=int, default=10000)
    parser.add_argument("--permutations", type=int, default=10000)
    args = parser.parse_args()
    try:
        if args.bootstrap_resamples <= 0 or args.permutations <= 0:
            raise ValueError("resample and permutation counts must be positive")
        data = json.loads(args.comparison.read_text(encoding="utf-8"))
        result = analyze(data, args.baseline, args.metric, args.seed, args.bootstrap_resamples, args.permutations)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        print(f"GENERATED: {args.output}")
        return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
