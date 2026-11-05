# Semantic ranking diagnosis

Status: preliminary engineering finding; not a publication claim.

## Finding

The first fallback benchmark showed BM25 outperforming semantic and equal-weight
hybrid retrieval. That result was not a semantic-quality result: the benchmark
was run without the pinned modern embedding cache.

The evaluator therefore used NEBULA's dependency-free fallback,
`hashing-v1-d128`, rather than `BAAI/bge-base-en-v1.5`. Feature hashing gives
useful deterministic behavior for correctness tests, but it is not a semantic
encoder and should not be used to tune the production hybrid weights.

On the private held-out split (843 documents, 8 queries), the fallback values
were:

| Variant | MRR | nDCG@5 | Recall@5 |
| --- | ---: | ---: | ---: |
| BM25 | 0.750000 | 0.739665 | 0.687500 |
| Equal-weight hybrid | 0.541667 | 0.588712 | 0.687500 |

After generating the pinned `BAAI/bge-base-en-v1.5` cache in the CPU-only
container, the same held-out split produced:

| Variant | MRR | nDCG@5 | Recall@5 |
| --- | ---: | ---: | ---: |
| BM25 | 0.750000 | 0.739665 | 0.687500 |
| BGE semantic | 0.812500 | 0.818531 | 0.812500 |
| BGE equal-weight hybrid | 0.875000 | 0.864665 | 0.812500 |

## Reproducibility boundary

The pinned model configuration is recorded in
`experiments/retrieval/modern-embedding-bge-base-en-v1.5.json`. The cache is
intentionally private and ignored by Git because it contains generated
vectors for the research corpus. The host preflight remains environment-limited,
but the supported CPU-only Docker path is now validated with
`tools/modern-embeddings-cpu-requirements.txt`. The private cache contains 891
vectors (843 documents plus the frozen query texts), with 768 dimensions and
the pinned model revision.

## Next action

The next research action is weight and candidate-depth analysis using the real
BGE cache. Any new ranking configuration must beat this BGE hybrid baseline on
validation and then be confirmed on the held-out split.
