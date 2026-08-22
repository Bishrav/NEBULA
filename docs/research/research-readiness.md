# NEBULA research-readiness dashboard

**Revision assessed:** `dfbfb6a`  
**Interpretation:** COMPLETE means supported by repository evidence; PARTIAL means infrastructure exists but the study evidence is incomplete; BLOCKED means external or missing data is required; NOT STARTED means no credible implementation exists.

| Requirement | Status | Evidence |
| --- | --- | --- |
| Research question | PARTIAL | [Research plan](research-plan.md); needs narrower primary contribution and formal constructs. |
| Engineering/research contribution boundary | NOT STARTED | To be added in Phase 1. |
| Ranking formula and configuration | PARTIAL | `HybridSearchEngine.java`, `TrustAwareSearchEngine.java`; weights are constructor defaults, not serialized. |
| Defensible trust definition | NOT STARTED | Current code uses authority, freshness, and graph signals without a formal construct. |
| Regression benchmark | COMPLETE | `benchmarks/evaluation/corpus-v1`, `queries-v1.psv`, CI runner. |
| Realistic public corpus | NOT STARTED | Current corpus has four synthetic Markdown files. |
| Sufficient research query set | NOT STARTED | Current set has 30 queries. |
| Multi-annotator relevance judgements | PARTIAL | Template, validator, agreement, and synthetic fixture exist; real labels are absent. |
| Weighted agreement | NOT STARTED | Current tool computes nominal Cohen’s kappa only. |
| Development/validation/test split | PARTIAL | Train/held-out split exists; validation split and split manifest are missing. |
| Strong baselines | PARTIAL | BM25, semantic, hybrid, authority, freshness, and trust variants exist; RRF and modern encoder are absent. |
| Trust-signal ablation | PARTIAL | Initial variants exist; full A0–A9 matrix is absent. |
| Weight sensitivity | NOT STARTED | No grid or robustness study exists. |
| Per-query and category analysis | PARTIAL | Error analysis exists; signal-level and category summaries are incomplete. |
| Statistical uncertainty | NOT STARTED | No bootstrap intervals, paired tests, effect sizes, or correction. |
| ANN scaling | NOT STARTED | Current benchmark has four documents and mean latency only. |
| Distributed experiments | PARTIAL | In-process and HTTP failure tests exist; multi-container measurements are absent. |
| Failure injection | NOT STARTED | No deterministic latency/error injection harness. |
| Reproducible manifests | PARTIAL | Checksums and Git revision exist; runtime/config/hardware/dirty-tree fields are missing. |
| Publication tables and plots | PARTIAL | CSV/Markdown tables exist; plotting and uncertainty outputs are missing. |
| Literature positioning | NOT STARTED | No verified related-work map. |
| Claim ledger | NOT STARTED | To be added in Phase 1. |
| Human study | BLOCKED | Requires supervisor/ethics review, recruitment, consent, and real data. |
| Ethics review | BLOCKED | Institutional determination is external. |
| Paper | NOT STARTED | Must wait for sufficient corpus, labels, experiments, and analysis. |

## Publication gate

NEBULA must not be called publication-ready until the NOT STARTED items that support the primary research question are complete, independently checked, and represented by versioned evidence. Until then, the safe label is **research prototype / preliminary study**.

