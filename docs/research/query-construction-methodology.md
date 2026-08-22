# NEBULA research query-construction methodology

This document defines how a future research query set must be built without turning document titles into artificial questions or manufacturing relevance judgements.

## Research-corpus-v1 candidate pool

`tools/generate_candidate_queries.py` creates a deterministic candidate pool from admitted document headings and normalized text. It paraphrases topics into information-need questions, records the grounding document and normalized checksum, and marks every candidate `NEEDS_HUMAN_REVIEW`. It does not create relevance judgements, select a final test set, or claim that a candidate is realistic.

The current pool contains 400 candidates across all 13 target categories. Human reviewers must reject title-like, vague, duplicate, context-dependent, or non-actionable questions using `metadata/query-review-sheet.csv`. The validator rejects duplicate IDs/text, malformed questions, unknown categories, premature approval, and unknown grounding documents.

## Target information needs

The initial research set should cover configuration, debugging, architecture, deployment, API usage, incident response, migration, security, database operations, observability, version compatibility, performance, and networking. The final category counts must be reported rather than assumed to be balanced.

## Query-writing rules

Each query should:

1. describe a realistic engineering information need;
2. be answerable, partially answerable, or unanswerable from the frozen corpus;
3. avoid copying a document title or distinctive sentence;
4. preserve the terminology an engineer would plausibly use;
5. record its category, construction source, version context, and rationale in protected metadata;
6. remain unchanged after relevance annotation begins, unless a new query-set version is registered.

Bad example: `PostgreSQL replication`.  
Better example: `How should I configure PostgreSQL streaming replication for automatic failover?`

## Construction process

1. Freeze and validate a licensed public corpus and its provenance manifest.
2. Have a query author sample information needs across the target categories.
3. Have a second reviewer check ambiguity, answerability, title leakage, and category assignment.
4. Freeze query IDs and wording before showing ranking outputs to annotators.
5. Obtain independent graded relevance judgements from at least two annotators.
6. Generate deterministic development, validation, and test assignments and record the split manifest.
7. Keep query-author notes, rejected queries, and adjudication records outside public raw qrels unless they are reviewed for privacy and licensing.

## What is not acceptable

- Counting retrieved documents as relevant without independent judgement.
- Using test-set failures to rewrite queries or tune ranking weights.
- Presenting a small synthetic regression fixture as a representative engineering benchmark.
- Claiming category balance, answerability, or statistical power without reporting the observed counts.
