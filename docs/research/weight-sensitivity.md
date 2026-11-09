# Weight sensitivity experiment

`com.nebula.evaluation.WeightSensitivityEvaluator` evaluates a baseline trust-
oriented configuration and 15 one-factor perturbations. Each perturbation
changes one of lexical, semantic, authority, freshness, or graph weight to
`0.0`, `0.20`, or `0.50`, proportionally rescales the other weights, and records
the exact resulting configuration.

Example:

```powershell
java -cp build/classes com.nebula.evaluation.WeightSensitivityEvaluator `
  benchmarks/evaluation/corpus-v1 `
  benchmarks/evaluation/queries-v1.psv `
  benchmarks/evaluation/trust-v1.psv `
  reports/generated/weight-sensitivity.json `
  5 1700000000000 `
  --embedding-cache reports/generated/pinned-bge-cache.tsv `
  --graph-links reports/generated/derived-corpus-graph-v1.psv
```

The output includes Precision@k, Recall@k, MRR, and NDCG@k for each grid row.
Current output from the synthetic regression fixture is descriptive only and
is labelled `REGRESSION_FIXTURE_ONLY`; it cannot establish robustness on the
research corpus beyond the current narrow known-item run. The protected split
is frozen, but its four-document packet qrels are incomplete and are not
sufficient to support a broad corpus-wide sensitivity claim.

Research-corpus runs must use a pinned embedding cache, resolved graph input,
and an explicit evaluation timestamp. A single-review authority file may be
used for provisional diagnostics, but authority sensitivity is not an
independent research result until a second reviewer completes the same file.
