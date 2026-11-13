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

On the historical private held-out split (843 documents, 8 queries), the fallback values
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

The tables above are historical diagnostics, not the current held-out result.
The pinned model configuration is recorded in
`experiments/retrieval/modern-embedding-bge-base-en-v1.5.json`. The cache is
intentionally private and ignored by Git because it contains generated
vectors for the research corpus. The host preflight remains environment-limited,
but the supported CPU-only Docker path is now validated with
`tools/modern-embeddings-cpu-requirements.txt`. The private cache contains 891
vectors (843 documents plus the frozen query texts), with 768 dimensions and
the pinned model revision.

## Next action

The fresh 16-query pooled run is now the current held-out result: pinned
BGE-hybrid NDCG@5 was 0.8544 versus 0.7165 for BM25. This remains limited
known-item/source-page evidence. A3-A9 also ran with reviewed authority
metadata and a derived graph, but the ablation is not isolated because the
corpus has uniform authority metadata and pooled coverage is incomplete. The
candidate is recorded in
`experiments/retrieval/bge-hybrid-selected.json`; runtime defaults remain
unchanged until a broader evaluation supports promotion.
