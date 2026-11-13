# Fresh held-out query authoring

This package is for creating a new sealed test set. It must not be populated
by copying, paraphrasing, or selecting from the existing 47-query review batch.
The author must not inspect NEBULA retrieval results, pooled qrels, or weight
sensitivity outputs while writing or revising these queries.

## Minimum target

Create at least 12 queries, with observed coverage reported across:

- both Kubernetes and PostgreSQL;
- multiple information-need categories;
- intermediate and advanced difficulty;
- freshness-sensitive and non-freshness-sensitive needs.

Do not force equal category counts after authoring. Report the actual counts.

## Required authoring process

1. Select a realistic engineering information need from the frozen public
   corpus, not a document title.
2. Write the query in natural language as an engineer would ask it.
3. Record the grounding document only in protected author metadata. It is not
   a relevance label and must not be injected into the retrieval pool.
4. Record why the query is answerable or partially answerable from the corpus,
   its version context, and whether freshness matters.
5. Have a second reviewer check ambiguity, title leakage, duplication,
   answerability, category, project, and difficulty.
6. Assign fresh IDs that do not overlap any existing `candidate-*` IDs.
7. Freeze wording and compute checksums before any retrieval or annotation.

## Leakage controls

- Keep author notes, rejected queries, grounding mappings, and the sealed test
  manifest outside the public repository until the study is complete.
- Do not use the old 8-query test IDs or any of the existing 47 public query
  texts.
- Do not tune ranking weights, select systems, or rewrite queries after seeing
  fresh test results.
- Do not create qrels from grounding documents or retrieval output.

## Validation

The public candidate validator can check schema, duplicate text, malformed
questions, categories, and grounding-document IDs. It does not prove that a
query is genuinely fresh or that the author avoided test leakage; those checks
remain a protected study-process responsibility.

Use the empty schema template in
`fresh-heldout-query-authoring-template.psv`. Every row must remain
`NEEDS_HUMAN_REVIEW` until the separate review and freeze process is complete.
