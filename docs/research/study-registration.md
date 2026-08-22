# NEBULA exploratory study registration template

This template records the study decisions that must be fixed before participant sessions begin. It is a planning and reproducibility document, not institutional ethics approval. Obtain the required university review, supervisor approval, consent language, and data-protection determination before recruitment.

## Study identity

| Field | Value |
| --- | --- |
| Working title | Trust-Aware Hybrid Retrieval for Explainable Engineering Knowledge Search |
| Principal investigator | __________________ |
| Supervisor / institution | __________________ |
| Ethics or review reference | __________________ |
| Registration date | __________________ |
| Study wave | __________________ |
| Protocol version | __________________ |
| Planned analysis commit | __________________ |

## Research question and hypotheses

Primary question: does exposing lexical, semantic, hybrid, and trust-aware ranking signals help engineers find and verify technical sources more effectively?

- H1: hybrid retrieval improves retrieval quality relative to BM25 on held-out labelled queries.
- H2: freshness and authority signals reduce stale or low-authority selections.
- H3: evidence and ranking explanations improve confidence and verification behavior.
- H4: HNSW reduces retrieval latency while maintaining acceptable recall against exact cosine search.

The hypotheses are exploratory until the corpus, labels, protocol, and analysis plan are frozen and the study is completed.

## Design and participants

- Design: within-participant exploratory search study with a fixed corpus and task script.
- Target participants: engineers or advanced computing students who regularly search technical documentation.
- Planned sample: 5–10 participants for the pilot; this is not powered for population-level inference.
- Inclusion criteria: __________________
- Exclusion criteria: __________________
- Recruitment channel: __________________
- Compensation: __________________
- Withdrawal process: participants may stop without penalty; withdrawal and deletion handling: __________________

Do not collect names, emails, account identifiers, unrelated browsing data, or sensitive workplace documents in NEBULA telemetry. Keep contact and consent records separate from session codes.

## Fixed materials

Record immutable versions before the first session:

- Corpus version: __________________
- Query-set version: __________________
- Query metadata version: __________________
- Task sheet version: __________________
- NEBULA commit: __________________
- API configuration / ranking modes: __________________
- Consent summary version: __________________

Any change creates a new protocol or study-wave version and must be recorded before continuing.

## Measures and analysis

Primary exploratory outcomes are task success, time to first useful source, reformulation count, and verification confidence. Secondary outcomes are useful-feedback rate, zero-result rate, evidence-preview use, and explanation inspection.

Use participant/session as the grouping unit, report medians and distributions for time measures, and show all completed and failed tasks. If within-participant mode comparisons are sufficiently populated, report paired differences with confidence intervals and state the small-sample limitation. Do not claim statistical significance unless the design and sample support it.

Predeclare exclusions: __________________

## Privacy, retention, and release

- Raw telemetry storage location and access list: __________________
- Retention/deletion date: __________________
- Redaction reviewer: __________________
- Publication derivative format: __________________
- Data-sharing decision and rationale: __________________

Raw exports must remain outside Git. Public releases should contain synthetic or explicitly consented data only, plus the checksum-backed experiment manifest.

## Readiness gate

- [ ] Ethics/supervisor review completed or documented as not required.
- [ ] Consent summary approved.
- [ ] Recruitment text approved.
- [ ] Task wording and rubric frozen.
- [ ] Corpus, query set, metadata, and code commit frozen.
- [ ] Pilot preflight and dry run pass.
- [ ] Raw-data access and deletion plan recorded.
- [ ] Annotation guide and adjudication process approved.
