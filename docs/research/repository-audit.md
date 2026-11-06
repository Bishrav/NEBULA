# NEBULA research-grade repository audit

**Audit date:** 2026-11-05
**Audited revision:** `906fb1c`
**Scope:** repository source, benchmarks, research documents, tests, CI, Docker configuration, and research tooling

## Executive assessment

NEBULA is a substantial engineering prototype with unusually good coverage of retrieval, trust-related metadata, graph ranking, approximate nearest-neighbour search, replication, failure handling, and reproducible research tooling. A public 843-document corpus, separately completed blinded annotation exports, frozen splits, and one held-out run now exist; the study is still not a publication-ready empirical study because the held-out sample is small.

The limiting factor is evidence, not feature count. The current benchmark contains 4 synthetic corpus documents and 30 labelled queries. The current results are useful regression evidence, but they cannot support general claims about engineering search quality, trust, user benefit, scalability, or statistical superiority. The first research phase therefore formalises the scientific object of study and prevents the existing product language from overclaiming.

## Component audit

| Component | Implemented? | Tested? | Research-ready? | Problem | Required fix |
| --- | --- | --- | --- | --- | --- |
| Repository and README | Yes | CI indirectly | Partial | Strong engineering overview, but prior wording mixed implemented, planned, and externally blocked work. | Keep an explicit implemented/evaluated/preliminary/planned distinction. |
| Research question and plan | Yes | Document review | Partial | The question is plausible but combines retrieval, trust, explanations, HNSW, distributed systems, and human study. | Narrow the primary contribution to trust-oriented hybrid retrieval; make secondary questions conditional. |
| Study registration | Template | No human study | Partial | Pilot sample is explicitly exploratory and not powered; ethics fields remain blank. | Preserve as a pre-registration template; do not present as approval or evidence. |
| Annotation guide | Yes | Python validation/tests | Partial | Two-annotator workflow, weighted agreement, blind workflow metadata, and adjudication tooling exist; the protected exports cover only four candidates per query. | Expand candidate coverage or use a corpus-wide relevance design before making publication-grade claims. |
| Corpus | Yes | Loader/tests | Partial | The public snapshot contains 843 attributed Kubernetes/PostgreSQL documents with checksums; licensing review is approved with attribution, while redistribution boundaries remain explicit. | Preserve notices and document any institution-specific redistribution constraints. |
| Query set | Yes | Validator/tests | Partial | Forty-seven approved queries are frozen in protected storage; the 8-query held-out subset is too small for broad inference. | Expand the independently reviewed query set for a stronger study. |
| Data splits | Complete (protected) | Gate/manifest checks | Partial | Development, validation, and held-out split tooling plus an immutable disjoint manifest are frozen for 47 approved queries. | Repeat on a larger approved batch before publication. |
| Trust metadata | Yes | Loader/tests | No | Authority is an input number and freshness is verification age; neither estimates truth. | Define reliability-related observables, provenance, calibration, missingness, and attack limits. |
| Lexical retrieval | Yes | Unit/integration tests | Partial | BM25 and phrase behaviour are deterministic. | Add stronger baselines and per-query exported results on the research corpus. |
| BM25 | Yes | Yes | Partial | Useful baseline, but parameter configuration is implicit and not serialized with results. | Centralize and persist all ranking parameters. |
| Semantic retrieval | Yes | Yes | No | Hashing and character n-gram embeddings are transparent baselines, not modern semantic encoders. | Add a reproducible modern embedding baseline when environment/licensing is settled. |
| Hybrid retrieval | Yes | Yes | Partial | Candidate-set min-max normalization is documented as a limitation; RRF and per-query outputs are now available. | Validate score-domain behaviour on the research corpus. |
| Ranking normalization | Yes | Unit coverage | No | Normalization is private implementation logic and has no public invariant suite for bounds or missing channels. | Extract tested normalization components with explicit failure behaviour. |
| Freshness ranking | Yes | Unit coverage | Partial | Exponential half-life is deterministic, but temporal appropriateness is not the same as trust. | Define freshness-sensitive query labels and measure stale-result effects separately. |
| Source authority | Yes | Unit/integration coverage | No | Authority is manually supplied and potentially subjective; provenance is not documented. | Define authority categories and provenance; analyze authority bias and manipulation. |
| PageRank / graph ranking | Yes | Tests/health benchmark | No | Graph health is measured, but relevance contribution is not evaluated on a larger linked corpus. | Add graph-aware ablation and popularity-bias fixtures. |
| Trust-aware ranking | Yes | Tests | No | Current formula combines lexical, authority, graph, and freshness signals, but calls them trust without a formal construct. | Rename or qualify as trust-oriented/evidence-aware and document the exact formula. |
| Evaluation metrics | Yes | Tests | Partial | Precision@k, Recall@k, MRR, NDCG, per-query records, paired intervals, permutation tests, effect sizes, and Holm correction are implemented; one constrained four-candidate held-out run is recorded. | Rebuild qrels with broader candidate coverage before interpreting corpus-wide metrics. |
| Error analysis | Yes | Tests/report output | Partial | Per-query errors, category summaries, variant deltas, and failure classifications are generated for the synthetic fixture; protected held-out analysis is recorded separately. | Expand signal-contribution analysis on a larger judged batch. |
| RRF baseline | Yes | Tests | Partial | Reciprocal Rank Fusion is implemented and included in the ranking comparison; the protected held-out comparison is preliminary. | Repeat the comparison on a larger frozen test set. |
| Learned ranking | No | No | Not yet | Dataset is too small for a credible learned model. | Defer until real labels and a frozen validation split exist. |
| Ablation framework | Yes | Variant evaluator tests | Partial | Explicit A0-A9 variants and legacy baselines run with stable identifiers, but no protected held-out A3-A9 results are reported; the current held-out comparison is BM25 versus BGE-hybrid only. | Run and report the full matrix on a larger approved qrels set. |
| Weight sensitivity | Yes | Evaluator/CI | Partial | Deterministic development-only 16-row grid is reproduced and validated; a larger protected research-corpus sensitivity run remains future work. | Run after the research batch is expanded. |
| HNSW | Yes | Unit/ANN tests | No | Four-document hashing benchmark reports mean latency only; no scale, p95/p99, build/memory, or parameter curves. | Build a separate 10K+ synthetic scalability benchmark and label it ANN systems evidence. |
| Exact cosine | Yes | ANN tests | Partial | Correct small baseline exists. | Share benchmark protocol and report repeated trials and resource measurements. |
| Distributed coordinator | Yes | Unit/in-process/network tests | Partial | Dedicated six-node Compose topology, deterministic fault controls, and observation runner now exist; only a bounded local scenario subset is measured. | Complete the scenario matrix and repeated-topology measurements. |
| Sharding | Yes | Tests/runner | Partial | Consistent hashing, fan-out, healthy/degraded top-k overlap, and local latency observations are covered. | Expand repeated multi-node measurements. |
| Replication | Yes | Tests/runner | Partial | Primary/replica failover and local D0/D1/D3/D8 recovery observations are documented. | Add broader recovery timing and topology replication. |
| Retries/circuit breaker | Yes | Tests/runner | Partial | Bounded retries, circuit state, and explicit partial responses are measured in selected local scenarios. | Complete latency/fault matrix coverage. |
| Metrics | Yes | Tests/API/runner | Partial | Prometheus-compatible health metrics and experiment-level latency, retry, circuit, partial-result, and overlap fields are exported. | Add per-shard failover counters if they become primary claims. |
| Generated reports | Yes | CI checks | Partial | Reports include per-query metrics, paired uncertainty, publication tables/figures, sensitivity, category failures, and explicit evidence boundaries; protected artifacts are kept outside the public tree. | Add a larger public-safe summary when the study batch is expanded. |
| Experiment manifest | Yes | Unit tests | Partial | Checksums and Git revision are recorded. | Add dirty-tree status, config, split/qrels versions, runtime, hardware, seed, and model metadata. |
| Research tooling | Yes | Python unit tests/CI | Partial | Validation, agreement, adjudication, statistics, plots, sensitivity, category analysis, distributed observation, and publication exports are present and exercised on protected artifacts. | Expand integration coverage as the protected study grows. |
| Java tests | Yes | 39 test files; CI compiles all and runs a focused subset | Partial | Many core invariants are covered, but research-critical split, normalization, RRF, statistics, and scaling tests are absent. | Add tests before changing each research-critical component. |
| Python tests | Yes | CI | Partial | Tooling tests cover current workflows. | Extend for manifests, splits, weighted agreement, statistics, and report claims. |
| CI | Yes | Yes | Partial | Build, tests, Docker, small benchmark, and tooling checks run on pushes/PRs. | Add split integrity and deterministic research smoke tests; keep large benchmarks separate. |
| Docker | Yes | Build/Compose validation | Partial | One-service and dedicated six-node research Compose topologies are available; local D0/D1/D3/D8 measurements are documented. | Complete the remaining scenarios and keep production claims bounded. |
| Literature positioning | Partial | Document review | Partial | A related-work map and contribution-boundary document exist; broader source-backed review remains. | Expand and verify the literature map before making novelty claims. |
| Claim control | Yes | Claim ledger/CI validation | Partial | A claim ledger, evidence-status language, and validation tooling constrain unsupported claims. | Keep the ledger synchronized with new measurements. |

## Highest-risk findings

1. **Construct validity:** “trust” currently describes a weighted combination of observable metadata signals; it does not estimate truth or source correctness.
2. **Benchmark validity:** the current 4-document/30-query fixture cannot support generalisation, statistical significance, or product claims.
3. **Evaluation leakage risk:** the protected development/validation/held-out split and qrels are frozen for the current batch; the main remaining risk is the small held-out sample.
4. **Fusion validity:** hybrid min-max normalization is calculated over each query’s candidate set, so the same raw score can receive different normalized values across queries.
5. **Statistical validity:** paired bootstrap intervals, permutation tests, effect sizes, and Holm correction exist for the synthetic fixture; adequately powered human evidence is absent.
6. **ANN evidence:** HNSW is demonstrated as a small deterministic implementation, not yet as a meaningful recall-latency scaling study.
7. **Distributed evidence:** a repeatable six-node topology and local D0/D1/D3/D8 measurements now exist, but the full matrix and production-scale evidence remain incomplete.
8. **Corpus provenance:** the 843-document corpus has provenance, checksums, and approved-with-attribution licensing review; public redistribution and institutional handling must continue to follow the recorded notices.

## Safe current conclusion

NEBULA is a strong research prototype and a credible infrastructure for an empirical study. At this revision it should be described as **preliminary systems research with protected held-out evidence**, not as a validated retrieval method or statistically supported trust claim.

