# Professor outreach email

Replace the bracketed fields before sending. Keep the wording factual and do
not describe NEBULA as a TU Darmstadt project or as an approved human study.

## Subject

Research discussion: NEBULA - explainable hybrid search and reproducible evaluation

## Draft

Dear Professor [Surname],

My name is Bishrav Shiwakoti, and I am an independent software engineer
interested in research on information retrieval, trustworthy AI systems, and
reproducible evaluation. I am writing to ask whether you would be open to a
short discussion about a project I am developing called NEBULA.

NEBULA is a Java 17 research prototype for engineering-knowledge search. It
combines BM25, semantic retrieval, hybrid fusion, HNSW, graph signals, source
evidence, and failure-aware distributed retrieval. The project is deliberately
inspectable: its research question, ranking configuration, evaluation tooling,
claim ledger, and limitations are documented in the repository.

The current evidence includes a public 843-document Kubernetes/PostgreSQL
corpus, protected queries and four-candidate labels, two separately completed
blinded annotation exports, a frozen development/validation/held-out split,
and one preliminary 8-query held-out run. Each packet contained one grounding
page and three seeded random same-project distractors; it was not produced by
system retrieval. The annotation packages were distributed separately, and
annotators did not see each other's labels or ranking-system outputs. The
held-out result therefore measures known-item/source-page identification over
the public corpus against a narrow judged packet, not corpus-wide graded
relevance. It is intentionally reported
as underpowered and descriptive; I am not presenting it as publication-grade
evidence or as proof of user benefit, statistical superiority, or a general
trust claim.

I would especially value guidance on narrowing the primary research question,
choosing defensible baselines and estimands, and determining whether the work
could become a suitable research project or dissertation direction at TU
Darmstadt. I would be happy to share a short review brief and demonstrate the
local reproducible workflow.

Project repository: https://github.com/Bishrav/NEBULA
Portfolio: https://portfolio-self-phi-65.vercel.app/

Thank you for your time. If this topic is outside your area, I would also be
grateful for a recommendation of the appropriate research group or colleague.

Kind regards,

Bishrav Shiwakoti
[Email address]
[Optional: LinkedIn / ORCID]
