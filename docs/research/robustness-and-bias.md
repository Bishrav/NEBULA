# Robustness and bias fixtures

The versioned [scenario matrix](../../experiments/robustness/scenario-matrix.json)
defines deterministic failure cases for observable trust-related signals. It
does not treat authority, freshness, or graph importance as truth labels.

The seven scenarios cover conflicting signals, exact-token versus semantic
retrieval, new-document discoverability, and manipulated metadata. Each future
run must record ranking order, per-result signal contributions, rank deltas
against the unmodified fixture, the complete configuration, and a manifest.

## Required comparisons

For each scenario compare the baseline metadata with the changed metadata under
the same query, corpus, timestamp, candidate cutoff, and ranking configuration.
Report:

- top-k rank changes;
- score-contribution changes;
- whether the relevant/correct fixture document was suppressed or promoted;
- sensitivity to modest weight changes;
- whether the change is attributable to authority, freshness, graph, lexical,
  or semantic signals.

The matrix is currently `NOT_YET_MEASURED`. These fixtures are diagnostic
evidence about system sensitivity, not evidence of population-level bias or
truthfulness. Real-world bias claims require a larger, independently judged
corpus and a pre-specified analysis.
