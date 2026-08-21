# NEBULA Evaluation Dataset v1

This directory contains a small synthetic engineering corpus and a labelled query set for regression testing. It is intentionally versioned and public-safe; it contains no private customer data.

## Format

`queries-v1.psv` uses pipe-separated rows:

```text
query_id|query|source_path:relevance_grade;source_path:relevance_grade
```

Relevance grades are integer values where `0` means not relevant and larger values indicate stronger relevance. The evaluator currently treats grades greater than zero as relevant for Precision@k, Recall@k, and MRR, while NDCG uses the grade magnitude.

Before evaluation, the dataset validator requires unique query IDs, non-empty queries, at least one positive judgement per query, source paths that exist in the corpus, and grades in the closed interval `0..3`. Explicit grade-0 judgements are retained as hard negatives.

## Run

After compiling the Java sources, run:

```powershell
java -cp .\algorithms\lexical\out com.nebula.evaluation.EvaluationRunner `
  .\benchmarks\evaluation\corpus-v1 `
  .\benchmarks\evaluation\queries-v1.psv `
  .\benchmarks\evaluation\trust-v1.psv `
  .\benchmarks\evaluation\reports\latest.md `
  .\benchmarks\evaluation\reports\latest.json
```

The runner compares BM25, semantic, hybrid lexical-semantic retrieval, authority-only, freshness-only, and combined trust-aware ranking. It reports each variant's metrics and deltas versus BM25 using a fixed evaluation timestamp, then writes Markdown and machine-readable JSON artifacts containing the same comparison. Embedding experiments should keep the corpus, query set, cutoff, and timestamp fixed while changing only the `EmbeddingModel` supplied to `SearchCatalog`.

When a report path is supplied, the runner also writes `ann-benchmark.md` and, when a JSON comparison path is supplied, `ann-benchmark.json`. This compares custom HNSW recall@k and mean query latency with exact cosine search over the same corpus, model, and cutoff. Latency is a local engineering measurement; recall is the primary regression signal.

The runner also writes `graph-benchmark.md` and `graph-benchmark.json`, recording graph size, dangling nodes, PageRank convergence, score-mass conservation, and deterministic top authority nodes.

The distributed baseline is currently exercised as an in-process coordinator test. It validates deterministic hash placement, fan-out merging, and explicit partial-result reporting when a shard is unavailable. It does not yet provide network transport or replication.

Replication tests validate synchronous primary/replica indexing, primary failover, and explicit reporting when both copies are unavailable.

The network coordinator test uses the existing HTTP search API, verifies result merging from a live endpoint, and confirms that an unreachable endpoint becomes an explicit partial-result failure after bounded retries.

When a report path is supplied, the runner also writes `embedding-ablation.md` beside it. This compares hashing and character n-gram embeddings in both semantic-only and hybrid retrieval modes.

The dataset is a starting regression fixture, not a publication-quality benchmark. Its documents include deterministic cross-links so PageRank and graph-aware trust ranking have a measurable signal. Future research versions must document corpus construction, query creation, annotator agreement, and split strategy.

## Annotation protocol

Each query should represent a realistic engineering-information need rather than a document title. Annotators assign `3` when a document directly answers the need, `2` when it provides substantial supporting evidence, `1` when it is useful context, and `0` when it is a plausible but non-answering result. Queries with no positive document are excluded from ranking evaluation.

For a publication study, freeze a query set before tuning ranking parameters, use at least two independent annotators, measure agreement, adjudicate disagreements, and keep held-out queries for final reporting. The current v1 fixture is a regression dataset and should not be presented as a statistically representative sample.
