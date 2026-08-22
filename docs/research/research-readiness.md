# NEBULA research-readiness dashboard

**Revision assessed:** `dfbfb6a`  
**Interpretation:** COMPLETE means supported by repository evidence; PARTIAL means infrastructure exists but the study evidence is incomplete; BLOCKED means external or missing data is required; NOT STARTED means no credible implementation exists.

| Requirement | Status | Evidence |
| --- | --- | --- |
| Research question | PARTIAL | [Research plan](research-plan.md); needs narrower primary contribution and formal constructs. |
| Engineering/research contribution boundary | COMPLETE | [Contribution boundaries](contribution-boundaries.md). |
| Ranking formula and configuration | COMPLETE | [Ranking model](ranking-model.md) and versioned JSON configuration. |
| Defensible trust definition | COMPLETE | [Ranking model](ranking-model.md) separates relevance, reliability signals, freshness, authority, graph importance, and evidence quality. |
| Regression benchmark | COMPLETE | `benchmarks/evaluation/corpus-v1`, `queries-v1.psv`, CI runner. |
| Realistic public corpus | PARTIAL | Provisional `datasets/research-corpus-v1` contains 843 documents from fixed Kubernetes/PostgreSQL commits with raw/normalized checksums and reports; human licensing review remains required. |
| Sufficient research query set | PARTIAL | 400 corpus-grounded candidate information needs exist across 13 categories; all require human review and no qrels/final split exist. |
| Multi-annotator relevance judgements | PARTIAL | Blinded A01/A02 packets, anonymous templates, validator, weighted agreement, disagreement, and guarded qrels tooling exist; human labels are absent. |
| Weighted agreement | PARTIAL | Agreement tool now reports nominal, linear weighted, and quadratic weighted kappa; real independently labelled qrels are absent. |
| Development/validation/test split | PARTIAL | Deterministic three-way split tool and manifest now exist; no research query set has been split yet. |
| Strong baselines | PARTIAL | BM25, hashing, character n-gram, hybrid, RRF, authority, freshness, and trust variants exist; BGE modern-cache adapter is configured but not yet evaluated on human qrels. |
| Trust-signal ablation | PARTIAL | Legacy variants and explicit A3–A9 hybrid signal combinations now run; real research results and A0–A2 labels still require the expanded benchmark. |
| Weight sensitivity | NOT STARTED | Requires development-set grid execution; no results are claimed. |
| Per-query and category analysis | PARTIAL | Error analysis exists; signal-level and category summaries are incomplete. |
| Statistical uncertainty | PARTIAL | Paired bootstrap intervals, permutation p-values, effect sizes, and Holm correction tooling now exist; no adequately powered research result exists. |
| ANN scaling | PARTIAL | Configurable synthetic vector-scale harness now records recall, p50/p95/p99 latency, build time, and heap delta; large runs are not yet measured. |
| Distributed experiments | PARTIAL | In-process and HTTP failure tests exist; multi-container measurements are absent. See [distributed plan](distributed-experiment-plan.md). |
| Failure injection | PARTIAL | Deterministic latency, HTTP-500, and connection-refusal controls plus regression tests exist; load and multi-container measurements are absent. |
| Reproducible manifests | PARTIAL | Checksums and Git revision exist; runtime/config/hardware/dirty-tree fields are missing. |
| Publication tables and plots | PARTIAL | CSV/Markdown tables exist; plotting and uncertainty outputs are missing. |
| Literature positioning | PARTIAL | [Related-work map](related-work-map.md) covers established methods; broader literature review remains. |
| Claim ledger | COMPLETE | [Claim ledger](claim-ledger.md) constrains wording and prohibits unsupported claims. |
| Human study | BLOCKED | Requires supervisor/ethics review, recruitment, consent, and real data. |
| Ethics review | BLOCKED | Institutional determination is external. |
| Paper | NOT STARTED | Must wait for sufficient corpus, labels, experiments, and analysis. |

## Publication gate

NEBULA must not be called publication-ready until the NOT STARTED items that support the primary research question are complete, independently checked, and represented by versioned evidence. Until then, the safe label is **research prototype / preliminary study**.
