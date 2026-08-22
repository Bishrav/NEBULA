# Request for Academic Approval and Supervision

**Project:** NEBULA — Trust-Aware Hybrid Retrieval for Explainable and Fresh Engineering Knowledge Search  
**Applicant:** [Your full name]  
**Programme / Department:** [Programme and department]  
**University:** [University name]  
**Date:** [Date]

**To:** [Professor’s full name]  
**[Professor’s title and department]**  
**[University name]**

**Subject: Request for approval and supervision of the NEBULA research-based software project**

Dear Professor [Surname],

I am writing to request your academic review, approval, and—if you consider it suitable—supervision of my research-based software project, **NEBULA**. The project is an open-source, reproducible search system designed for engineering knowledge bases, where a useful result should be not only relevant but also current, authoritative, explainable, and resilient to distributed-system failures.

## Project overview

Conventional search systems commonly optimise lexical or semantic relevance in isolation. In engineering environments, this can produce results that are technically related but stale, weakly sourced, difficult to verify, or unavailable during a shard or service failure. NEBULA investigates whether hybrid retrieval combined with explicit trust signals can improve the quality and verifiability of engineering knowledge search.

The proposed research question is:

> Does adding freshness, source-authority, graph, and evidence signals to hybrid lexical-semantic retrieval improve retrieval quality and user verifiability compared with lexical-only, semantic-only, and conventional hybrid baselines?

The project is innovative as an integrated research and engineering system rather than as a claim that any one individual algorithm is new. It connects retrieval evaluation, trust-aware ranking, graph authority, evidence presentation, reproducibility controls, and failure-aware distributed search in one inspectable implementation.

## Current implementation status

The repository currently contains a working research prototype with:

- BM25, semantic hashing, exact vector, HNSW, and hybrid retrieval baselines;
- freshness, source-authority, graph/PageRank, and trust-aware ranking signals;
- evidence-oriented result explanations and deterministic evaluation reports;
- in-process distributed coordination using consistent hashing and fan-out result merging;
- replicated shard storage, bounded retry behaviour, health monitoring, circuit breaking, and partial-result reporting;
- a Docker deployment path, Prometheus-compatible metrics, automated tests, and continuous integration;
- versioned synthetic corpus, query labels, benchmark artifacts, experiment manifests, and research-report generation;
- annotation validation, inter-annotator agreement analysis, adjudication support, and a release-quality gate;
- a frozen development/held-out query split to reduce evaluation leakage during future tuning.

The current evaluation materials are deliberately described as a synthetic engineering fixture and regression baseline. I will not present them as human-subject evidence or as proof of generalisation. The repository records limitations, reproducibility requirements, and the distinction between measured implementation behaviour and future research claims.

## Proposed academic study

Following approval, I propose to conduct the study in the following stages:

1. Freeze the corpus, query set, labels, configurations, and evaluation protocol.
2. Compare lexical-only, semantic-only, hybrid, and trust-aware variants using ranking metrics such as Precision@k, Recall@k, MRR, and NDCG.
3. Measure engineering trade-offs including latency, HNSW recall, index size, and failure-recovery behaviour.
4. Run ablation studies for freshness, authority, graph, and evidence signals.
5. If approved and feasible, conduct a small user evaluation of result verification speed and confidence using informed consent, anonymised records, and an ethics-approved protocol.
6. Analyse failure cases, annotator agreement, limitations, and threats to validity before preparing a dissertation chapter or publication manuscript.

The principal hypotheses are that hybrid retrieval will improve ranking quality over BM25 alone, trust signals will reduce stale or weakly sourced results, and explicit evidence will improve users’ ability to verify a result. These hypotheses will remain open until the relevant experiments are completed.

## Ethics, privacy, and scope

No human-subject study, participant recruitment, personal data collection, or publication submission will begin without the university’s required approval and guidance. The current repository uses public-safe synthetic data only. Any future human evaluation will use the university’s prescribed consent, storage, anonymisation, withdrawal, and data-retention procedures. I will also seek guidance on whether the proposed user evaluation requires ethics review or an exemption determination.

## Request for your review

I would be grateful if you could:

- review whether NEBULA is suitable as a university research project;
- advise on the research question, evaluation design, and expected academic contribution;
- confirm whether you would be willing to supervise or recommend an appropriate supervisor;
- advise on ethics approval, participant recruitment, and data-management requirements; and
- suggest any changes needed before I begin the formal study phase.

The source repository, technical documentation, research plan, reproducibility artifacts, and current implementation are available for review at:

**Repository:** https://github.com/Bishrav/NEBULA

I have attached or can provide the system architecture, research plan, evaluation protocol, current benchmark reports, and a demonstration of the running prototype. I would welcome the opportunity to discuss the project and revise its scope in accordance with your guidance.

Thank you for considering this request.

Yours sincerely,

[Your full name]  
[Student ID, if applicable]  
[Programme / Department]  
[University name]  
[Email address]  
[Phone number, optional]

## Suggested attachments

- NEBULA project repository and README
- System architecture and deployment documentation
- NEBULA research plan and study-registration draft
- Evaluation and annotation protocol
- Current synthetic benchmark reports and experiment manifest
- Demonstration URL or local run instructions
