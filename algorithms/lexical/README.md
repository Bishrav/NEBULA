# NEBULA Lexical Index

The lexical index is the first search-engine component in NEBULA.

## Current capabilities

- Locale-stable lowercase tokenization
- Unicode letter and number token support
- Positional posting lists
- Term frequency and document frequency
- Document length and average document length statistics
- Duplicate document protection
- Deterministic in-memory indexing
- BM25 ranking with configurable `k1` and `b`
- Stable top-k ordering
- Per-term score contributions for explainability

The initial analyzer intentionally does not remove stop words or stem terms. Those policies will be evaluated against a labelled query set rather than introduced without evidence.

## BM25 baseline

The current ranker uses:

```text
IDF(t) = log(1 + (N - df(t) + 0.5) / (df(t) + 0.5))

score(D, Q) = sum(IDF(t) * TF_normalized(t, D))
```

The default parameters are `k1 = 1.2` and `b = 0.75`. Each result exposes the contribution of every matched query term so later freshness, authority, and semantic signals can be evaluated separately.
