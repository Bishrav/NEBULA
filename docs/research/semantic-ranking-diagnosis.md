# Semantic ranking diagnosis

Status: preliminary engineering finding; not a publication claim.

## Finding

The first frozen human-qrels benchmark shows BM25 outperforming the current
semantic and equal-weight hybrid variants. This result is expected to be
treated as a baseline only because the benchmark was run without the pinned
modern embedding cache.

The evaluator therefore used NEBULA's dependency-free fallback,
`hashing-v1-d128`, rather than `BAAI/bge-base-en-v1.5`. Feature hashing gives
useful deterministic behavior for correctness tests, but it is not a semantic
encoder and should not be used to tune the production hybrid weights.

On the private held-out split (843 documents, 8 queries), the observed
baseline values were:

| Variant | MRR | nDCG@5 | Recall@5 |
| --- | ---: | ---: | ---: |
| BM25 | 0.750000 | 0.739665 | 0.687500 |
| Equal-weight hybrid | 0.541667 | 0.588712 | 0.687500 |

## Reproducibility boundary

The pinned model configuration is recorded in
`experiments/retrieval/modern-embedding-bge-base-en-v1.5.json`. The cache is
intentionally private and ignored by Git because it contains generated
vectors for the research corpus. The modern embedding preflight currently
reports `ENVIRONMENT_LIMITED` on the development machine: the required
`sentence-transformers` and `torch` packages are not installed, and Docker's
Linux engine is unavailable.

## Next action

Prepare a compatible isolated Python environment, run the frozen input
manifest through `tools/generate_modern_embeddings.py`, and rerun development,
validation, and held-out evaluation with `--embedding-cache`. Only after that
measurement should NEBULA tune semantic/hybrid weights or make a claim about
semantic retrieval quality.
