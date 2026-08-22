"""Run repeatable coordinator observations for the distributed scenario matrix.

Fault state is controlled outside this process (Compose stop/restart or
explicit NEBULA_FAULT_* environment variables). This keeps the observation
runner separate from the system under test.
"""

import argparse
import csv
import json
import statistics
import time
import urllib.parse
import urllib.request
from pathlib import Path


def percentile(values, fraction):
    ordered = sorted(values)
    if not ordered:
        return None
    index = min(len(ordered) - 1, int((len(ordered) - 1) * fraction + 0.999999))
    return ordered[index]


def top_k_overlap(healthy_ids, degraded_ids, k):
    left = set(healthy_ids[:k])
    right = set(degraded_ids[:k])
    return len(left & right) / float(k) if k else 0.0


def ndcg(ids, relevance, k):
    ranked = ids[:k]
    ideal = sorted(relevance.values(), reverse=True)[:k]

    def gain(value, position):
        import math
        return (2 ** value - 1) / math.log2(position + 2)

    actual = sum(gain(relevance.get(doc_id, 0), index) for index, doc_id in enumerate(ranked))
    best = sum(gain(value, index) for index, value in enumerate(ideal))
    return actual / best if best else 0.0


def request(base_url, query, limit):
    target = base_url.rstrip("/") + "/v1/search?" + urllib.parse.urlencode({"q": query, "limit": limit})
    started = time.perf_counter_ns()
    with urllib.request.urlopen(target, timeout=30) as response:
        payload = json.loads(response.read().decode("utf-8"))
    payload["_clientLatencyNanos"] = time.perf_counter_ns() - started
    return payload


def run(args):
    observations = []
    for repeat in range(args.repeats):
        payload = request(args.base_url, args.query, args.limit)
        observations.append({
            "repeat": repeat,
            "partial": payload.get("partial", True),
            "failedShards": payload.get("failedShards", []),
            "resultIds": [item["documentId"] for item in payload.get("results", [])],
            "latencyMillis": payload["_clientLatencyNanos"] / 1_000_000.0,
            "serverLatencyMillis": payload.get("latencyNanos", 0) / 1_000_000.0,
            "metrics": payload.get("metrics", {}),
        })

    healthy_ids = []
    if args.baseline:
        healthy_ids = json.loads(args.baseline.read_text(encoding="utf-8")).get("resultIds", [])
    elif args.scenario == "D0":
        healthy_ids = observations[0]["resultIds"]
    for observation in observations:
        observation["topKOverlap"] = top_k_overlap(healthy_ids, observation["resultIds"], args.limit) if healthy_ids else None

    latency = [item["latencyMillis"] for item in observations]
    summary = {
        "schemaVersion": "distributed-result-v1",
        "scenario": args.scenario,
        "query": args.query,
        "limit": args.limit,
        "repeats": args.repeats,
        "status": "MEASURED",
        "throughputPerSecond": args.repeats / (sum(latency) / 1000.0) if sum(latency) else None,
        "p50LatencyMillis": percentile(latency, 0.50),
        "p95LatencyMillis": percentile(latency, 0.95),
        "p99LatencyMillis": percentile(latency, 0.99),
        "partialResultFrequency": sum(item["partial"] for item in observations) / float(args.repeats),
        "topKOverlap": statistics.mean([item["topKOverlap"] for item in observations if item["topKOverlap"] is not None]) if healthy_ids else None,
        "ndcgDegradation": "NOT_MEASURED" if not args.qrels else None,
        "observations": observations,
    }
    if args.qrels:
        relevance = json.loads(args.qrels.read_text(encoding="utf-8"))
        degraded = statistics.mean(ndcg(item["resultIds"], relevance, args.limit) for item in observations)
        summary["degradedNdcg"] = degraded
        summary["ndcgDegradation"] = "requires healthy qrels baseline"
    return summary


def write_outputs(summary, output):
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
    csv_path = output.with_suffix(".csv")
    fields = ["scenario", "repeat", "partial", "failedShards", "latencyMillis", "serverLatencyMillis", "topKOverlap", "retryAttempts", "shardFailures", "openCircuits"]
    with csv_path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        for item in summary["observations"]:
            metrics = item["metrics"]
            writer.writerow({
                "scenario": summary["scenario"], "repeat": item["repeat"], "partial": item["partial"],
                "failedShards": "|".join(item["failedShards"]), "latencyMillis": item["latencyMillis"],
                "serverLatencyMillis": item["serverLatencyMillis"], "topKOverlap": item["topKOverlap"],
                "retryAttempts": metrics.get("retryAttempts", 0), "shardFailures": metrics.get("shardFailures", 0),
                "openCircuits": metrics.get("openCircuits", 0),
            })
    markdown_path = output.with_suffix(".md")
    markdown_path.write_text(
        "# Distributed experiment result\n\n"
        f"- Scenario: '{summary['scenario']}'\n- Status: '{summary['status']}'\n"
        f"- p50/p95/p99 latency: '{summary['p50LatencyMillis']}' / '{summary['p95LatencyMillis']}' / '{summary['p99LatencyMillis']}' ms\n"
        f"- Partial-result frequency: '{summary['partialResultFrequency']}'\n"
        f"- Top-k overlap: '{summary['topKOverlap']}'\n"
        f"- NDCG degradation: '{summary['ndcgDegradation']}'\n\n"
        "This result is topology- and hardware-specific distributed-systems evidence.\n",
        encoding="utf-8",
    )


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", required=True)
    parser.add_argument("--scenario", required=True)
    parser.add_argument("--query", required=True)
    parser.add_argument("--limit", type=int, default=10)
    parser.add_argument("--repeats", type=int, default=10)
    parser.add_argument("--baseline", type=Path)
    parser.add_argument("--qrels", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    if args.limit <= 0 or args.repeats <= 0:
        raise ValueError("limit and repeats must be positive")
    write_outputs(run(args), args.output)
    print(f"GENERATED: {args.output}")


if __name__ == "__main__":
    main()
