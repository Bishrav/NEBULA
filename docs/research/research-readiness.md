# NEBULA research-readiness dashboard

**Revision assessed:** `0c33b08`
**Interpretation:** COMPLETE means supported by repository evidence; PARTIAL means infrastructure exists but the study evidence is incomplete; BLOCKED means external or missing data is required; NOT STARTED means no credible implementation exists.

| Requirement | Status | Evidence |
| --- | --- | --- |
| Research question | COMPLETE | [Research plan](research-plan.md) and the README use the same narrow primary question: how freshness, source-authority, and graph-related signals affect retrieval effectiveness relative to lexical, dense, and conventional hybrid retrieval for engineering knowledge. Human-evidence requirements remain incomplete, so the research result itself is not yet validated. |
| Engineering/research contribution boundary | COMPLETE | [Contribution boundaries](contribution-boundaries.md). |
| Ranking formula and configuration | COMPLETE | [Ranking model](ranking-model.md) and versioned JSON configuration. |
| Defensible trust definition | COMPLETE | [Ranking model](ranking-model.md) separates relevance, reliability signals, freshness, authority, graph importance, and evidence quality. |
| Regression benchmark | COMPLETE | `benchmarks/evaluation/corpus-v1`, `queries-v1.psv`, CI runner. |
| Realistic public corpus | PARTIAL | Provisional `datasets/research-corpus-v1` contains 843 documents from fixed Kubernetes/PostgreSQL commits with raw/normalized checksums and reports; human licensing review remains required. |
| Sufficient research query set | PARTIAL | 400 corpus-grounded candidate information needs exist across 13 categories; all require human review and no qrels/final split exist. |
| Multi-annotator relevance judgements | BLOCKED | The checkout contains a 400-candidate query pool, but no reproducible 379-query/3,790-pair retrieval artifact or human qrels. The blinded pipeline is prepared for a protected retrieval artifact; human labels, agreement, adjudication, and qrels remain absent. |
| Weighted agreement | PARTIAL | Agreement tool now reports nominal, linear weighted, and quadratic weighted kappa; real independently labelled qrels are absent. |
| Development/validation/test split | PARTIAL | Stratified 60/20/20 freeze tool, immutable manifest, held-out CLI guard, and final-evaluation gate exist; final split is blocked until approved queries and human qrels exist. |
| Strong baselines | PARTIAL | BM25, hashing, character n-gram, hybrid, RRF, authority, freshness, trust variants, and the pinned BGE cache path exist; BGE is evaluated on the regression fixture but not yet on human qrels. |
| Trust-signal ablation | PARTIAL | Legacy variants and explicit A3–A9 hybrid signal combinations now run; real research results and A0–A2 labels still require the expanded benchmark. |
| Weight sensitivity | PARTIAL | Deterministic one-factor 16-row evaluator and regression-fixture artifact are implemented; research-corpus sensitivity remains blocked by human qrels and frozen splits. |
| Per-query and category analysis | PARTIAL | Error analysis exists; signal-level and category summaries are incomplete. |
| Statistical uncertainty | PARTIAL | Paired bootstrap intervals, permutation p-values, effect sizes, and Holm correction tooling now exist; no adequately powered research result exists. |
| ANN scaling | PARTIAL | Configurable repeated-trial harness and manifest tooling exist; 10K and a lower-cost 100K run are measured, with the 100K configuration showing zero recall; target-parameter 100K and 1M runs are HARDWARE-LIMITED. Results are synthetic-vector systems evidence only. |
| Distributed experiments | PARTIAL | Dedicated coordinator plus three primary/replica pairs, corrected counter-delta runner, and local D0–D8 measurements exist; qrels-based NDCG degradation and broader topology replication remain. See [distributed plan](distributed-experiment-plan.md). |
| Failure injection | PARTIAL | Deterministic latency/HTTP-500 controls are available through explicit environment variables, with stop-container controls documented for unavailable endpoints; load and multi-container measurements are absent. |
| Reproducible manifests | PARTIAL | [Reproducibility package](reproducibility-package.md) and checksum-backed manifest tooling record inputs, Git revision, dirty-tree status, runtime, platform, architecture, processor, and CPU count; full research-run integration remains. |
| Publication tables and plots | PARTIAL | Dependency-free CSV, LaTeX, plot-data, SVG, and distributed-summary generation now exists; research-corpus figures and uncertainty outputs remain blocked by human qrels. |
| Literature positioning | PARTIAL | [Related-work map](related-work-map.md) covers established methods; broader literature review remains. |
| Claim ledger | COMPLETE | [Claim ledger](claim-ledger.md) constrains wording and prohibits unsupported claims. |
| Human study | BLOCKED | Draft submission package now exists in docs/research/human-study with protocol, participant materials, consent, risk, data-management, task, questionnaire, debriefing, analysis, and checklist documents; supervisor/institutional review, recruitment, consent, and real data remain outstanding. |
| Ethics review | BLOCKED | Institutional determination is external. |
| Final held-out evaluation | BLOCKED | Final-evaluation gate stops execution until corpus licensing, approved queries, human qrels/agreement, frozen split, final model/configuration, and clean-tree checks pass. |
| Paper | NOT STARTED | Must wait for sufficient corpus, labels, experiments, and analysis. |

## Publication gate

NEBULA must not be called publication-ready until the NOT STARTED items that support the primary research question are complete, independently checked, and represented by versioned evidence. Until then, the safe label is **research prototype / preliminary study**.
