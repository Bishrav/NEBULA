#!/usr/bin/env python3
"""Generate a reproducible technical report and publication-friendly tables."""

import argparse
import csv
import json
from datetime import datetime, timezone
from pathlib import Path


def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def write_csv(path, headers, rows):
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(headers)
        writer.writerows(rows)


def number(value, digits=4):
    return f"{value:.{digits}f}"


def build_report(benchmark, ann, graph, manifest, generated_at):
    variants = benchmark["variants"]
    best = max(variants, key=lambda row: row["ndcgAtK"])
    tied = [row["name"] for row in variants if row["ndcgAtK"] == best["ndcgAtK"]]
    authority = next((row["ndcgAtK"] for row in variants if row["name"] == "authority_only"), None)
    freshness = next((row["ndcgAtK"] for row in variants if row["name"] == "freshness_only"), None)
    tied_text = ", ".join(tied)
    ablation_text = ""
    if authority is not None and freshness is not None:
        ablation_text = f" Authority-only and freshness-only report NDCG@{benchmark['cutoff']} of {authority:.4f} and {freshness:.4f}, respectively."
    return f"""# NEBULA Retrieval Research Report

## Technical summary

On the fixed synthetic evaluation fixture, the best NDCG@{benchmark['cutoff']} was {best['ndcgAtK']:.4f}, achieved by: {tied_text}. HNSW matched exact cosine retrieval at recall@{ann['cutoff']} of {ann['hnswRecallAtK']:.4f}, while PageRank converged after {graph['iterations']} iterations and conserved score mass ({graph['scoreMass']:.4f}). These are reproducibility and regression results, not evidence of general user or production performance.

## Key findings from the controlled fixture

### Lexical, semantic, and hybrid ranking are tied on this fixture

The ranking comparison contains {benchmark['corpusDocuments']} documents and {benchmark['queryCount']} labelled queries at cutoff @{benchmark['cutoff']}. The leading variants are {tied_text}.{ablation_text} The fixture therefore validates deterministic execution and exposes ablation behavior, but it does not establish general ranking superiority.

### HNSW matches exact retrieval while remaining an explicit ANN baseline

HNSW recall@{ann['cutoff']} is {ann['hnswRecallAtK']:.4f} against exact cosine search. Mean measured latency is {ann['hnswMeanLatencyMs']:.6f} ms for HNSW and {ann['exactMeanLatencyMs']:.6f} ms for exact search in this small local run. The latency comparison is descriptive only; it should not be generalized beyond this machine, corpus, or implementation configuration.

### The linked document graph is healthy and convergent

The graph contains {graph['nodes']} nodes and {graph['edges']} directed edges, with {graph['danglingNodes']} dangling nodes. PageRank converged and conserved score mass at {graph['scoreMass']:.6f}. Top nodes by score: {', '.join(graph['topNodes'])}. Graph authority is now measurable, but graph-ranking quality still requires larger linked corpora and held-out relevance labels.

## Scope and metric definitions

- **Corpus:** {benchmark['corpusDocuments']} synthetic Markdown documents.
- **Queries:** {benchmark['queryCount']} fixed labelled queries with graded relevance judgments.
- **Ranking metrics:** Precision@k, Recall@k, MRR, and NDCG@k; BM25 is the comparison baseline.
- **ANN recall:** overlap between HNSW and exact cosine top-k results, divided by exact top-k size.
- **Graph health:** directed node/edge counts, dangling nodes, PageRank convergence, and total score mass.
- **Evaluation time:** fixed timestamp `{benchmark['evaluationNowEpochMillis']}` for ranking freshness calculations.

## Methodology and reproducibility

The evaluation runner indexes the versioned corpus, loads the versioned query and trust datasets, evaluates each ranking variant at the same cutoff, and writes machine-readable artifacts. HNSW uses the deterministic hashing embedding model, fixed index parameters, and a fixed seed. The graph benchmark rebuilds links from ingested Markdown and computes PageRank with the repository defaults. The experiment manifest records SHA-256 checksums for all inputs and the Git revision `{manifest.get('gitRevision', 'unknown')}`.

## Limitations and robustness checks

This fixture is synthetic, small, and not a representative sample of engineering search. It does not establish statistical significance, user benefit, production latency, or superiority of any ranking method. Latency is hardware- and JVM-dependent. The current embedding model is a transparent hashing baseline rather than a learned language model. The primary robustness checks are deterministic reruns, exact-vs-HNSW recall comparison, graph score-mass conservation, and checksum verification; real research requires corpus expansion, held-out queries, multiple annotators, and confidence intervals.

## Recommended next steps

1. Expand the corpus and query set with documented construction and held-out evaluation splits.
2. Run independent annotation and agreement checks before making retrieval-quality claims.
3. Add repeated latency trials and memory/index-size measurements for ANN comparisons.
4. Conduct the supervised pilot, then join user outcomes to the same versioned experiment manifest.

## Further questions

- Does trust-aware ranking improve verification speed or confidence when lexical and semantic relevance disagree?
- How does HNSW recall change as corpus size, dimension, and `efSearch` increase?
- Which link-graph construction rules produce useful authority signals without amplifying documentation popularity bias?

_Generated at {generated_at}. See the companion CSV tables and experiment manifest for audit-ready artifacts._
"""


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--benchmark", type=Path, required=True)
    parser.add_argument("--ann-benchmark", type=Path, required=True)
    parser.add_argument("--graph-benchmark", type=Path, required=True)
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--tables-dir", type=Path, required=True)
    parser.add_argument("--generated-at")
    args = parser.parse_args()
    benchmark, ann, graph, manifest = (load(args.benchmark), load(args.ann_benchmark), load(args.graph_benchmark), load(args.manifest))
    generated_at = args.generated_at or datetime.now(timezone.utc).replace(microsecond=0).isoformat()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(build_report(benchmark, ann, graph, manifest, generated_at), encoding="utf-8")
    write_csv(args.tables_dir / "ranking-comparison.csv",
              ["variant", "precision_at_k", "recall_at_k", "mrr", "ndcg_at_k", "delta_ndcg_vs_bm25"],
              [[row["name"], row["precisionAtK"], row["recallAtK"], row["mrr"], row["ndcgAtK"], row["deltaNdcgVsBm25"]] for row in benchmark["variants"]])
    write_csv(args.tables_dir / "ann-comparison.csv", ["method", "recall_at_k", "mean_latency_ms"],
              [["exact_cosine", ann["exactRecallAtK"], ann["exactMeanLatencyMs"]], ["hnsw", ann["hnswRecallAtK"], ann["hnswMeanLatencyMs"]]])
    write_csv(args.tables_dir / "graph-health.csv", ["nodes", "edges", "dangling_nodes", "iterations", "converged", "score_mass"],
              [[graph["nodes"], graph["edges"], graph["danglingNodes"], graph["iterations"], graph["converged"], graph["scoreMass"]]])
    print(f"GENERATED: {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
