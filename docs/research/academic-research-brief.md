# NEBULA: Academic Research Brief

**Working title:** Trust-Oriented Hybrid Retrieval for Explainable and Fresh Engineering Knowledge Search  
**Status:** Research proposal and prototype overview  
**Repository:** [github.com/Bishrav/NEBULA](https://github.com/Bishrav/NEBULA)

> This brief describes a research direction and an implemented prototype. It does not claim that the final empirical study has been completed.

## Abstract

Engineering teams depend on documentation that changes over time and is distributed across many sources. Conventional lexical search can miss semantically related information, while opaque semantic systems may provide relevant-looking results without making their evidence, provenance, freshness, or failure behaviour clear.

NEBULA investigates whether observable signals—freshness, source authority, graph structure, and evidence metadata—can improve engineering-knowledge retrieval when combined with lexical and semantic baselines. The project implements a transparent retrieval core with BM25, semantic baselines, exact vector search, a custom HNSW index, hybrid ranking, Reciprocal Rank Fusion, graph authority, and trust-oriented reranking. It also evaluates the reliability of retrieval under shard, replica, and network failures.

The central contribution is not a new BM25, embedding, HNSW, or PageRank algorithm. It is a reproducible experimental framework for measuring how these observable signals interact in engineering search, while keeping ranking decisions and limitations inspectable.

## Motivation

Technical knowledge is not only a text-matching problem. An engineering result may be lexically relevant but obsolete, poorly sourced, disconnected from the current system, or unavailable when part of the search service fails. A useful search system should therefore expose more than a relevance score:

- what evidence matched the information need;
- which observable signals affected the ranking;
- how current and attributable the source is;
- whether graph context changes the result;
- whether the answer is complete or degraded because of infrastructure failures.

The project treats “trust” as a research label for observable ranking signals, not as an estimate of truth. This distinction is important: a source-authority value or freshness timestamp can be measured, but neither proves that a document is correct.

## Research question

**How do observable freshness, source-authority, graph, and evidence-related signals affect retrieval effectiveness compared with lexical-only, semantic-only, and conventional hybrid retrieval for engineering knowledge?**

### Secondary questions

1. Which signals help or hurt, and for which categories of engineering information needs?
2. How sensitive are results to modest changes in ranking weights?
3. What recall/latency trade-off does HNSW exhibit as vector scale and search parameters increase?
4. How do shard and replica failures affect latency, completeness, and top-k result quality?

The ANN and distributed questions are secondary systems evaluations. They must not be used to imply improved relevance.

## Hypotheses

The following are falsifiable hypotheses for the future research evaluation:

- **H1 — hybrid retrieval:** A hybrid lexical-semantic ranker will outperform either lexical-only or semantic-only retrieval on at least some engineering query categories.
- **H2 — observable signals:** Adding freshness, source-authority, and graph signals will change retrieval quality in a query-category-dependent way; the direction and size of the effect must be measured rather than assumed.
- **H3 — robustness:** A ranking configuration selected on development data will retain its relative behaviour on a frozen held-out split under modest weight perturbations.
- **H4 — ANN trade-off:** HNSW will reduce query latency at a measurable recall cost compared with exact cosine search as corpus scale increases.
- **H5 — resilience:** Replication and bounded recovery mechanisms will improve result availability during controlled failures, while degraded responses may reduce completeness or latency.

## Proposed contribution

NEBULA’s defensible contribution is a combination of:

1. **An inspectable retrieval framework** for engineering documentation that keeps lexical, semantic, freshness, authority, graph, and evidence signals explicit.
2. **A controlled evaluation design** with simple baselines, ablations, development/held-out separation, per-query outputs, and reproducible input manifests.
3. **A systems evaluation track** for ANN and distributed retrieval that reports recall, latency, resource use, completeness, retries, failover, and partial-result behaviour together.
4. **A clear claim boundary** that separates prototype capability, regression evidence, preliminary measurements, and research conclusions.

The project does not claim novelty for BM25, dense retrieval, RRF, HNSW, PageRank, replication, retries, or circuit breaking.

## Prototype architecture

The current system is implemented primarily in Java 17, with Python research tooling and a Docker runtime:

```text
sources
  -> ingestion and normalisation
  -> lexical / vector / graph indexes
  -> shard query and local ranking
  -> coordinator merge
  -> trust-oriented reranking
  -> results with evidence and explanations
```

The repository includes:

- BM25 and phrase retrieval;
- transparent semantic hashing and character n-gram baselines;
- exact cosine and custom HNSW search;
- weighted hybrid fusion and RRF;
- freshness, source-authority, and graph/PageRank signals;
- sharding, replication, retries, circuit breaking, and partial-result reporting;
- a browser UI, HTTP API, evaluation runner, manifests, and generated reports.

## Experimental design

### Baselines and variants

The planned comparison includes:

1. BM25 lexical retrieval;
2. semantic retrieval;
3. weighted lexical-semantic fusion;
4. Reciprocal Rank Fusion;
5. authority-only and freshness-only variants;
6. trust-oriented combined ranking;
7. exact cosine versus HNSW;
8. healthy versus controlled degraded distributed scenarios.

Every experiment should serialize its corpus version, query version, labels, cutoff, ranking parameters, timestamp, random seed, model configuration, runtime, and hardware context.

### Data

The repository currently contains two distinct data tracks:

- a small synthetic regression fixture used for CI and behavioural regression;
- an attributed 843-document Kubernetes/PostgreSQL corpus snapshot with provenance metadata and a protected approved query set.

The corpus review is approved with attribution for the public corpus. The protected review batch contains 47 approved queries and four candidate labels per query: one grounding page plus three seeded random same-project distractors. Two separately completed blinded exports were compared, adjudicated, and used for a frozen development/validation/held-out split. Protected labels are intentionally excluded from the public checkout. One 8-query held-out run is available as preliminary source-page identification evidence within this constrained candidate pool; it is not exhaustive corpus-wide relevance evidence, and the synthetic fixture remains regression-only.

### Evaluation

The final study should report:

- Precision@k, Recall@k, MRR, and NDCG;
- per-query and per-category results;
- paired uncertainty intervals and effect sizes;
- weight-sensitivity and ablation results;
- HNSW recall, latency percentiles, build time, memory, and index size;
- distributed latency, completeness, retries, failover, circuit state, and partial-result rates;
- failure cases and examples where a signal helps or harms.

A final held-out study must occur only after corpus licensing review, query review, independent annotation, agreement analysis, adjudication, split freezing, and configuration freezing.

## Current evidence

The repository currently demonstrates:

- a working multi-baseline retrieval prototype;
- deterministic ranking and explicit failure paths;
- a custom ANN implementation with regression checks;
- reproducible synthetic evaluation with SHA-256 input locking;
- research-corpus acquisition and validation tooling;
- CI that compiles Java sources, runs tests, validates Python tooling, and builds the container.

## Current synthetic-fixture analysis

The current reproducible fixture contains 30 queries, 14 ranking variants, and 8 query
categories. On this fixture, conventional hybrid retrieval has mean NDCG@5 of
0.9325 versus 0.8033 for BM25; the paired mean difference is 0.1292 with a
bootstrap 95% interval of [0.0438, 0.2299]. The full NEBULA variant has a mean
difference of 0.1186 with an interval of [0.0301, 0.2241]. These are descriptive
fixture results only: they are not evidence of general superiority, statistical
significance, or user benefit.

The category analysis also shows why aggregate scores are insufficient. Some
variants improve on phrase-search queries while authority-only and
freshness-only variants lose against BM25 in several exact-terminology,
semantic-concept, and failure-response rows. The analysis records 69
variant/query failure or regression cases. These patterns motivate the planned
human-judged corpus study and should be treated as hypotheses to test, not
conclusions.

Generated artifacts are produced by tools/reproduce_benchmark.py under
reports/generated/regression-v1/, including paired statistics,
category-error-analysis.md, and category-summary.csv.

It does not yet demonstrate:

- human-perceived trust or user benefit;
- statistical superiority over established systems;
- generalisation to real engineering organisations;
- production-scale latency, memory, or availability;
- legally cleared research data or released human qrels.

## Risks and safeguards

| Risk | Safeguard |
| --- | --- |
| “Trust” is interpreted as truth | Define and report observable signals separately; use “trust-oriented” language |
| Corpus licensing is unclear | Preserve provenance and attribution; require human licensing review |
| Tuning leaks into held-out data | Freeze development/held-out split and enforce release gates |
| Metadata creates popularity or authority bias | Run signal ablations, missingness analysis, and adversarial metadata checks |
| Small fixture produces misleading results | Label it regression evidence and report no general claims |
| Explanation is mistaken for user-understood reasoning | Evaluate explanation usefulness separately from score decomposition |
| Failure recovery hides incomplete results | Return explicit partial-result metadata and measure completeness |

A human study, if pursued, requires appropriate supervision, institutional review, participant information, consent, data management, and an approved analysis plan. The draft materials in `docs/research/human-study/` are preparation documents, not approval or participant evidence.

## Proposed research plan

### Phase 1 — establish the evidence base

- Complete corpus licensing and attribution review.
- Review and freeze the candidate query protocol.
- Obtain independent relevance annotations under an approved process.
- Measure agreement and adjudicate disagreements.
- Freeze the research split and release gate.

### Phase 2 — run the retrieval study

- Compare BM25, semantic, hybrid, RRF, and trust-oriented variants.
- Run signal ablations and weight sensitivity only on development data.
- Evaluate per-query and per-category effects.
- Report uncertainty, effect sizes, and negative findings.

### Phase 3 — evaluate systems behaviour

- Run repeatable ANN scale experiments.
- Run multi-container distributed fault experiments.
- Measure latency, recall, completeness, recovery, and resource use.
- Separate relevance claims from availability and systems claims.

### Phase 4 — package the result

- Publish a reproducibility bundle with code, manifests, configurations, and generated tables.
- Update the claim ledger from measured evidence only.
- Write a short paper or technical report with related work and limitations.
- Present the prototype and open research questions to a potential supervisor.

## Supervision and collaboration topics

The project would benefit from academic guidance on:

- information-retrieval experiment design and baseline selection;
- relevance-judgement protocols and inter-annotator agreement;
- uncertainty analysis and statistical testing;
- trustworthy and explainable retrieval;
- robustness, bias, and metadata manipulation;
- distributed retrieval evaluation and reproducibility.

The intended request is feedback and supervision on a bounded empirical study, not endorsement of claims that have not yet been measured.

## Reproducibility checklist

A research release should include:

- source revision and clean-tree status;
- corpus manifest, license records, and checksums;
- query, label, and split versions;
- ranking and embedding configuration;
- runtime, hardware, seed, and dependency versions;
- per-query results and aggregate tables;
- uncertainty and ablation outputs;
- failure-injection configuration and logs;
- limitations and claim-ledger status.

Start with the [research plan](research-plan.md), [research-readiness dashboard](research-readiness.md), [contribution boundaries](contribution-boundaries.md), and [evaluation guide](../../benchmarks/evaluation/README.md).
