# Related-work map

This map records established methods that NEBULA uses as baselines or engineering components. It prevents those methods from being presented as novel.

| Area | Primary reference | Relation to NEBULA | Boundary |
| --- | --- | --- | --- |
| Probabilistic lexical retrieval / BM25 | Robertson & Zaragoza, [The Probabilistic Relevance Framework: BM25 and Beyond](https://www.nowpublishers.com/article/DownloadEBook/INR-019) | lexical baseline | NEBULA does not claim BM25 novelty |
| Dense retrieval | Karpukhin et al., [Dense Passage Retrieval](https://aclanthology.org/2020.emnlp-main.550/) | planned stronger semantic baseline | model choice and evaluation must be reported; no new encoder is claimed |
| Rank fusion | Cormack, Clarke & Büttcher, [Reciprocal Rank Fusion](https://research.google/pubs/reciprocal-rank-fusion-outperforms-condorcet-and-individual-rank-learning-methods/) | alternative to incompatible score fusion | RRF is an established baseline |
| Link authority | Brin & Page, [The Anatomy of a Large-Scale Hypertextual Web Search Engine](https://research.google/pubs/the-anatomy-of-a-large-scale-hypertextual-web-search-engine/) | graph-authority signal | PageRank is not a trust or truth estimator |
| Approximate nearest neighbours | Malkov & Yashunin, [HNSW](https://arxiv.org/abs/1603.09320) | ANN implementation benchmark | only recall/latency trade-offs are evaluated |
| Explainable retrieval | explanation quality must be evaluated separately from score decomposition | evidence-oriented explanation rubric | score transparency alone is not human-grounded explanation |
| Distributed information retrieval | coordinator, sharding, replication, retries, and circuit breaking are systems mechanisms | secondary reliability track | no novel distributed protocol is claimed |

## Positioning rule

The paper should compare NEBULA against these established techniques and report controlled effects. It should not describe combining known signals as algorithmic invention unless a literature review and evidence justify a narrower new method.
