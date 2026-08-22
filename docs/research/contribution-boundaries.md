# Contribution boundaries

NEBULA does not claim to invent BM25, dense retrieval, hybrid retrieval, Reciprocal Rank Fusion, HNSW, PageRank, replication, retries, or circuit breakers. These are established methods and engineering patterns.

The defensible contribution is narrower:

> an empirical framework for measuring how relevance, semantic similarity, freshness, source-authority metadata, graph authority, and evidence-oriented explanations interact in engineering-knowledge retrieval.

The work becomes a scientific contribution only if the expanded corpus, independently judged queries, held-out evaluation, ablations, uncertainty analysis, and failure analysis support a reproducible result. Until then, the safe description is **research prototype / preliminary study**.

## Research questions

1. How does trust-oriented hybrid retrieval compare with lexical, semantic, and conventional hybrid baselines on engineering information needs?
2. Which observable signals help or hurt retrieval quality, and for which query categories?
3. Is the ranking robust to modest weight changes and adversarial metadata?
4. What recall/latency trade-off does the ANN implementation exhibit as vector scale grows?
5. How do shard and replica failures affect latency, completeness, and top-k result quality?

Questions 4 and 5 are secondary systems evaluations; they must not be used to imply improved retrieval relevance.
