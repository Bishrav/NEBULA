# NEBULA publication claim ledger

This ledger prevents engineering features or preliminary fixture results from being presented as scientific findings. A claim may move to COMPLETE only when its evidence is generated from a frozen, sufficiently sized, independently judged, held-out experiment.

| Claim | Evidence required | Status | Safe wording now |
| --- | --- | --- | --- |
| NEBULA improves NDCG over BM25 | Held-out relevance benchmark, per-query paired analysis, uncertainty, and effect size | NOT YET MEASURED | “On the current synthetic regression fixture, the variants produce the reported descriptive metrics.” |
| Trust signals improve retrieval quality | Trust construct, labelled signal-relevant queries, full ablation, held-out analysis | NOT YET MEASURED | “NEBULA exposes configurable freshness, authority, and graph-related signals.” |
| Freshness reduces stale results | Valid stale-result definition and freshness-sensitive queries | NOT YET MEASURED | “Freshness is an observable ranking signal implemented with a configurable decay.” |
| Authority improves reliability | Provenance-backed authority categories and independent validity assessment | NOT YET MEASURED | “Authority metadata is an input feature; it is not a truth estimate.” |
| PageRank improves engineering retrieval | Larger linked corpus, graph ablation, relevance labels, popularity-bias analysis | NOT YET MEASURED | “PageRank is implemented and its graph-health properties are measured.” |
| Explanations improve user verification | Approved human study comparing conditions with task outcomes | HUMAN STUDY PENDING | “The interface exposes score components and evidence for future evaluation.” |
| HNSW reduces latency at acceptable recall | 10K+ scale benchmark, exact baseline, repeated trials, percentiles, resource measurements | NOT YET MEASURED | “The current HNSW implementation is a deterministic small-scale ANN baseline.” |
| NEBULA is robust to shard failures | Repeatable multi-container faults, overlap/NDCG degradation, latency and recovery metrics | NOT YET MEASURED | “Failure paths are covered by integration tests; distributed performance is not yet measured.” |
| NEBULA estimates trust | Validated target construct and calibration against external judgements | REJECTED CLAIM | “NEBULA is trust-oriented/evidence-aware: it combines observable, configurable signals and does not estimate truth.” |
| NEBULA introduces a new retrieval algorithm | Verified literature review and demonstrated methodological novelty | REJECTED CLAIM | “The contribution is a controlled empirical investigation of combined signals for engineering knowledge retrieval.” |

