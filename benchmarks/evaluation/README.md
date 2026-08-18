# NEBULA Evaluation Dataset v1

This directory contains a small synthetic engineering corpus and a labelled query set for regression testing. It is intentionally versioned and public-safe; it contains no private customer data.

## Format

`queries-v1.psv` uses pipe-separated rows:

```text
query_id|query|source_path:relevance_grade;source_path:relevance_grade
```

Relevance grades are integer values where `0` means not relevant and larger values indicate stronger relevance. The evaluator currently treats grades greater than zero as relevant for Precision@k, Recall@k, and MRR, while NDCG uses the grade magnitude.

## Run

After compiling the Java sources, run:

```powershell
java -cp .\algorithms\lexical\out com.nebula.evaluation.EvaluationRunner `
  .\benchmarks\evaluation\corpus-v1 `
  .\benchmarks\evaluation\queries-v1.psv `
  .\benchmarks\evaluation\trust-v1.psv `
  .\benchmarks\evaluation\reports\latest.md
```

The runner compares BM25, hybrid lexical-semantic retrieval, authority-only, freshness-only, and combined trust-aware ranking. It reports each variant's metrics and deltas versus BM25 using a fixed evaluation timestamp, then writes a Markdown report containing query-level error analysis. Embedding experiments should keep the corpus, query set, cutoff, and timestamp fixed while changing only the `EmbeddingModel` supplied to `SearchCatalog`.

The dataset is a starting regression fixture, not a publication-quality benchmark. Future research versions must document corpus construction, query creation, annotator agreement, and split strategy.
