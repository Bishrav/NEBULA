# NEBULA Evaluation Metrics

## Retrieval quality

- Precision@k: proportion of returned results that are relevant.
- Recall@k: proportion of known relevant documents that are returned.
- MRR: rank of the first relevant result.
- NDCG: graded relevance quality across the ranked list.

## Trust and usefulness

- Citation accuracy: whether cited passages support the result or answer.
- Stale-result rate: proportion of results that violate the freshness expectation.
- Evidence sufficiency: whether a user can verify the result from returned passages.
- Search success rate: whether the user finds a useful answer within the first k results.
- Time to verification: time needed to confirm an answer from its sources.

## Systems performance

- Indexing throughput: documents and tokens processed per second.
- Query latency: p50, p95, and p99 response time.
- Index size: bytes per document and total segment size.
- Memory usage: resident memory by index component.
- ANN recall@k: approximate-nearest-neighbour recall against exact search.
- Shard failure rate and partial-result rate.
- Recovery and snapshot-restore time.

## Reporting requirement

Every benchmark must state the dataset version, query-set version, hardware, configuration, software version, and whether the result is a cold or warm run.
