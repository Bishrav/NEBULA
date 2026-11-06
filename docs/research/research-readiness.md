# NEBULA research-readiness dashboard

**Revision assessed:** `252d058`
**Interpretation:** COMPLETE means supported by repository evidence; PARTIAL means infrastructure exists but the study evidence is incomplete; BLOCKED means external or missing data is required; NOT STARTED means no credible implementation exists.

| Requirement | Status | Evidence |
| --- | --- | --- |
| Research question | COMPLETE | [Research plan](research-plan.md) and the README use the same narrow primary question: how freshness, source-authority, and graph-related signals affect retrieval effectiveness relative to lexical, dense, and conventional hybrid retrieval for engineering knowledge. Human-evidence requirements remain incomplete, so the research result itself is not yet validated. |
| Engineering/research contribution boundary | COMPLETE | [Contribution boundaries](contribution-boundaries.md). |
| Ranking formula and configuration | COMPLETE | [Ranking model](ranking-model.md) and versioned JSON configuration. |
| Defensible trust definition | COMPLETE | [Ranking model](ranking-model.md) separates relevance, reliability signals, freshness, authority, graph importance, and evidence quality. |
| Regression benchmark | COMPLETE | `benchmarks/evaluation/corpus-v1`, `queries-v1.psv`, CI runner. |
| Realistic public corpus | COMPLETE (public corpus) | `datasets/research-corpus-v1` contains 843 public-repository documents from fixed Kubernetes/PostgreSQL commits with raw/normalized checksums and an approved-with-attribution licensing review; queries, labels, and qrels remain protected. |
| Sufficient research query set | PARTIAL | Review batch 01 has 47 approved queries in protected storage, with a frozen 30/9/8 split; the public repository retains the candidate-generation and review tooling, not protected query labels. |
| Multi-annotator candidate labels | PARTIAL (protected batch) | Two protected blinded exports validate with 188 rows each across four candidates per query; labels and qrels are intentionally excluded from the public repository, and the remaining corpus is unjudged. |
| Weighted agreement | DESCRIPTIVE ONLY (protected) | Recomputed agreement reports zero disagreements and kappa of 1.0 on the constrained four-candidate packet; this does not establish broad relevance-label quality. |
| Development/validation/test split | COMPLETE (protected) | The protected split manifest is frozen, disjoint, and requires `--final-heldout-evaluation`. |
| Strong baselines | PARTIAL (configuration frozen) | BGE, hybrid, and baseline results exist on the protected four-candidate qrels; final BGE ranking and embedding manifests are frozen from validation-only evidence, but corpus-wide relevance remains unmeasured. |
| Trust-signal ablation | PARTIAL | Legacy variants and explicit A0–A9 hybrid signal combinations are implemented and runnable, but no protected held-out A3–A9 results are reported; the current held-out comparison is BM25 versus BGE-hybrid only. |
| Weight sensitivity | PARTIAL | Deterministic one-factor 16-row evaluator and regression-fixture artifact are implemented; research-corpus sensitivity remains blocked by human qrels and frozen splits. |
| Per-query and category analysis | PARTIAL | Per-query failures and category summaries are generated for the synthetic fixture, with protected held-out analysis recorded separately; broader signal-level analysis remains incomplete. |
| Statistical uncertainty | PARTIAL | Paired bootstrap intervals, permutation p-values, effect sizes, and Holm correction tooling now exist; no adequately powered research result exists. |
| ANN scaling | PARTIAL | Configurable repeated-trial harness and manifest tooling exist; 10K and a lower-cost 100K run are measured, with the 100K configuration showing zero recall; target-parameter 100K and 1M runs are HARDWARE-LIMITED. Results are synthetic-vector systems evidence only. |
| Distributed experiments | PARTIAL | Local D0/D1/D3/D8 measurements now record latency, partial responses, retries, circuit observations, recovery, and top-k overlap; the remaining matrix, qrels-based quality degradation, and broader topology replication remain. See [distributed plan](distributed-experiment-plan.md). |
| Failure injection | PARTIAL | Deterministic latency/HTTP-500 controls are available through explicit environment variables, with stop-container controls documented for unavailable endpoints; load and multi-container measurements are absent. |
| Reproducible manifests | PARTIAL | [Reproducibility package](reproducibility-package.md) and checksum-backed manifest tooling record inputs, Git revision, dirty-tree status, runtime, platform, architecture, processor, and CPU count; full research-run integration remains. |
| Publication tables and plots | PARTIAL | Dependency-free CSV, LaTeX, plot-data, SVG, and distributed-summary generation now exists; research-corpus figures and uncertainty outputs remain blocked by human qrels. |
| Literature positioning | PARTIAL | [Related-work map](related-work-map.md) covers established methods; broader literature review remains. |
| Claim ledger | COMPLETE | [Claim ledger](claim-ledger.md) constrains wording and prohibits unsupported claims. |
| Human study | BLOCKED | Draft submission package now exists in docs/research/human-study with protocol, participant materials, consent, risk, data-management, task, questionnaire, debriefing, analysis, and checklist documents; supervisor/institutional review, recruitment, consent, and real data remain outstanding. |
| Ethics review | BLOCKED | Institutional determination is external. |
| Final held-out evaluation | PARTIAL | The protected gate passed and one frozen held-out run was measured on 8 queries with four labelled candidates per query; it tests source-page identification within the candidate pool, not corpus-wide relevance, and is too small for publication-grade inference. |
| Paper | NOT STARTED | Must wait for sufficient corpus, labels, experiments, and analysis. |

## Publication gate

NEBULA must not be called publication-ready until the NOT STARTED items that support the primary research question are complete, independently checked, and represented by versioned evidence. Until then, the safe label is **research prototype / preliminary study**.
