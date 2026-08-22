# NEBULA research-grade repository audit

**Audit date:** 2026-08-22  
**Audited revision:** `dfbfb6a`  
**Scope:** repository source, benchmarks, research documents, tests, CI, Docker configuration, and research tooling

## Executive assessment

NEBULA is a substantial engineering prototype with unusually good coverage of retrieval, trust-related metadata, graph ranking, approximate nearest-neighbour search, replication, failure handling, and reproducible small-fixture tooling. It is not yet a publication-ready empirical study.

The limiting factor is evidence, not feature count. The current benchmark contains 4 synthetic corpus documents and 30 labelled queries. The current results are useful regression evidence, but they cannot support general claims about engineering search quality, trust, user benefit, scalability, or statistical superiority. The first research phase therefore formalises the scientific object of study and prevents the existing product language from overclaiming.

## Component audit

| Component | Implemented? | Tested? | Research-ready? | Problem | Required fix |
| --- | --- | --- | --- | --- | --- |
| Repository and README | Yes | CI indirectly | Partial | Strong engineering overview, but prior wording mixed implemented, planned, and externally blocked work. | Keep an explicit implemented/evaluated/preliminary/planned distinction. |
| Research question and plan | Yes | Document review | Partial | The question is plausible but combines retrieval, trust, explanations, HNSW, distributed systems, and human study. | Narrow the primary contribution to trust-oriented hybrid retrieval; make secondary questions conditional. |
| Study registration | Template | No human study | Partial | Pilot sample is explicitly exploratory and not powered; ethics fields remain blank. | Preserve as a pre-registration template; do not present as approval or evidence. |
| Annotation guide | Yes | Python validation/tests | Partial | Two-annotator workflow exists, but only nominal kappa is implemented and the real qrels are absent. | Add weighted agreement, blind workflow metadata, adjudication traceability, and protected storage instructions. |
| Corpus | Yes | Loader/tests | No | Four Markdown documents are synthetic and intentionally public-safe. | Add a licensed, attributed public engineering corpus with source metadata and checksums. |
| Query set | Yes | Validator/tests | No | Thirty queries are useful regression cases but too small for publication claims. | Build a documented 100+ query research set without fabricating judgements. |
| Data splits | Partial | Count checks in CI | No | Development and held-out files exist; there is no validation split or split manifest with seed and code revision. | Add deterministic development/validation/test split metadata and integrity tests. |
| Trust metadata | Yes | Loader/tests | No | Authority is an input number and freshness is verification age; neither estimates truth. | Define reliability-related observables, provenance, calibration, missingness, and attack limits. |
| Lexical retrieval | Yes | Unit/integration tests | Partial | BM25 and phrase behaviour are deterministic. | Add stronger baselines and per-query exported results on the research corpus. |
| BM25 | Yes | Yes | Partial | Useful baseline, but parameter configuration is implicit and not serialized with results. | Centralize and persist all ranking parameters. |
| Semantic retrieval | Yes | Yes | No | Hashing and character n-gram embeddings are transparent baselines, not modern semantic encoders. | Add a reproducible modern embedding baseline when environment/licensing is settled. |
| Hybrid retrieval | Yes | Yes | No | Per-query min-max normalization makes weights dependent on candidate-set extrema; no RRF comparison exists. | Formalize score domains, add RRF, and export configuration and signal ranges. |
| Ranking normalization | Yes | Unit coverage | No | Normalization is private implementation logic and has no public invariant suite for bounds or missing channels. | Extract tested normalization components with explicit failure behaviour. |
| Freshness ranking | Yes | Unit coverage | Partial | Exponential half-life is deterministic, but temporal appropriateness is not the same as trust. | Define freshness-sensitive query labels and measure stale-result effects separately. |
| Source authority | Yes | Unit/integration coverage | No | Authority is manually supplied and potentially subjective; provenance is not documented. | Define authority categories and provenance; analyze authority bias and manipulation. |
| PageRank / graph ranking | Yes | Tests/health benchmark | No | Graph health is measured, but relevance contribution is not evaluated on a larger linked corpus. | Add graph-aware ablation and popularity-bias fixtures. |
| Trust-aware ranking | Yes | Tests | No | Current formula combines lexical, authority, graph, and freshness signals, but calls them trust without a formal construct. | Rename or qualify as trust-oriented/evidence-aware and document the exact formula. |
| Evaluation metrics | Yes | Tests | Partial | Precision@k, Recall@k, MRR, and NDCG are implemented; output is aggregate-first. | Add per-query records, confidence intervals, paired tests, effect sizes, and multiple-comparison correction. |
| Error analysis | Yes | Tests/report output | Partial | Query errors and returned ranks are produced, but signal-level help/hurt categories are not complete. | Add per-query variant deltas, contributions, and category summaries. |
| RRF baseline | No | No | No | Score fusion is the only hybrid comparison. | Implement Reciprocal Rank Fusion and compare against score fusion. |
| Learned ranking | No | No | Not yet | Dataset is too small for a credible learned model. | Defer until real labels and a frozen validation split exist. |
| Ablation framework | Partial | Variant evaluator tests | No | Authority-only, freshness-only, and a combined variant exist; the planned full matrix is missing. | Add explicit A0–A9 variants with stable configuration identifiers. |
| Weight sensitivity | No | No | No | Defaults are hand-selected and sensitivity is unknown. | Add a development-only grid runner and sensitivity outputs. |
| HNSW | Yes | Unit/ANN tests | No | Four-document hashing benchmark reports mean latency only; no scale, p95/p99, build/memory, or parameter curves. | Build a separate 10K+ synthetic scalability benchmark and label it ANN systems evidence. |
| Exact cosine | Yes | ANN tests | Partial | Correct small baseline exists. | Share benchmark protocol and report repeated trials and resource measurements. |
| Distributed coordinator | Yes | Unit/in-process/network tests | No | Failure behaviour is tested, but not with a repeatable multi-container topology or load measurements. | Add Docker Compose experiment harness and deterministic fault injection. |
| Sharding | Yes | Tests | Partial | Consistent hashing and fan-out are tested in process. | Measure healthy/degraded top-k overlap and latency under controlled topology. |
| Replication | Yes | Tests | Partial | Primary/replica failover is tested in process. | Add network/container scenarios, recovery timing, and explicit failure metrics. |
| Retries/circuit breaker | Yes | Tests | Partial | Bounded retries and circuit state are implemented. | Add injected latency, HTTP 500, refusal, intermittent failure, and recovery experiments. |
| Metrics | Yes | Tests/API | Partial | Prometheus-compatible endpoint and health snapshots exist. | Export experiment-level latency percentiles, retry, failover, circuit, and partial-result metrics. |
| Generated reports | Yes | CI checks | No | Reports are reproducible for the small fixture but contain some fixture-specific prose and no uncertainty. | Make reports data-driven, uncertainty-aware, and explicit about preliminary evidence. |
| Experiment manifest | Yes | Unit tests | Partial | Checksums and Git revision are recorded. | Add dirty-tree status, config, split/qrels versions, runtime, hardware, seed, and model metadata. |
| Research tooling | Yes | Python unit tests | Partial | Validation, agreement, adjudication, and reports are present. | Add standard experiment runner, statistics, plots, split integrity, and publication exports. |
| Java tests | Yes | 39 test files; CI compiles all and runs a focused subset | Partial | Many core invariants are covered, but research-critical split, normalization, RRF, statistics, and scaling tests are absent. | Add tests before changing each research-critical component. |
| Python tests | Yes | CI | Partial | Tooling tests cover current workflows. | Extend for manifests, splits, weighted agreement, statistics, and report claims. |
| CI | Yes | Yes | Partial | Build, tests, Docker, small benchmark, and tooling checks run on pushes/PRs. | Add split integrity and deterministic research smoke tests; keep large benchmarks separate. |
| Docker | Yes | Build/one-service validation | No | Compose runs one API with a synthetic corpus, not coordinator plus shards/replicas. | Add a separate research distributed topology and failure experiment workflow. |
| Literature positioning | No | No | No | No verified related-work map or contribution boundary document exists. | Build a source-backed map before making novelty claims. |
| Claim control | Partial | Human review only | No | Reports include limitations, but no claim ledger tracks evidence status. | Add claim ledger and enforce “not yet measured” language. |

## Highest-risk findings

1. **Construct validity:** “trust” currently describes a weighted combination of observable metadata signals; it does not estimate truth or source correctness.
2. **Benchmark validity:** the current 4-document/30-query fixture cannot support generalisation, statistical significance, or product claims.
3. **Evaluation leakage risk:** only development and held-out files exist; a validation split and split manifest are missing.
4. **Fusion validity:** hybrid min-max normalization is calculated over each query’s candidate set, so the same raw score can receive different normalized values across queries.
5. **Statistical validity:** no bootstrap intervals, paired tests, effect sizes, or multiple-comparison correction are implemented.
6. **ANN evidence:** HNSW is demonstrated as a small deterministic implementation, not yet as a meaningful recall-latency scaling study.
7. **Distributed evidence:** failure paths are tested, but distributed performance and partial-result quality are not measured in a repeatable multi-node experiment.
8. **Corpus provenance:** no public source collection, license/attribution record, or realistic engineering corpus exists yet.

## Safe current conclusion

NEBULA is a strong research prototype and a credible infrastructure for a future empirical study. At this revision it should be described as **preliminary systems research infrastructure**, not as a validated retrieval method or statistically supported trust claim.

