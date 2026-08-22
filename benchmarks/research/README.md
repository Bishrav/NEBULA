# NEBULA research corpus and query-set contract

This directory is reserved for a future public-safe research benchmark. It intentionally does not contain fabricated documents, queries, relevance judgements, or experimental results.

## Corpus admission requirements

Every admitted document must have a row in `corpus-manifest.template.psv` and a text file below the corpus root. The validator requires:

- a stable `document_id` and unique relative `text_path`;
- title, source, source type, project, version, and ISO-8601 creation/update dates;
- a public `https://` source URL;
- a license identifier and attribution text;
- an explicit authority category with documented provenance;
- a SHA-256 checksum of the exact text file;
- graph links that resolve to admitted document IDs, when links are recorded.

The source collector must retain the original URL, retrieval date, license/terms interpretation, and any transformation applied to the text. Restricted, private, paywalled, or unclear-license content must not be copied into the repository.

Validate an acquired corpus with:

```powershell
python tools/validate_corpus_manifest.py `
  --manifest benchmarks/research/corpus-manifest.psv `
  --corpus-dir benchmarks/research/corpus-v2
```

The validator checks structure and provenance consistency; it does not decide whether a license permits redistribution. A human legal/attribution review remains required.

## Query-set admission requirements

Queries must express an information need, not repeat a document title. Record the category and construction rationale outside the qrels. Recommended categories are configuration, debugging, architecture, deployment, API usage, incident response, migration, security, database operations, observability, version compatibility, performance, and networking.

Do not create relevance judgements by assuming that a retrieved document is correct. Freeze the query wording and corpus version first, then obtain independent judgements using the annotation workflow. The current `benchmarks/evaluation` fixture remains a regression benchmark and must not be relabelled as the research corpus.

Trust-conflict cases are specified in `trust-conflicts-v1.psv`. They define evaluation scenarios only; they are not relevance judgements or measured results.

The first acquired snapshot is under `datasets/research-corpus-v1`. Its manifest and reports are provisional until human licensing and attribution review is completed.

## Split policy

Use the deterministic split tool only after query IDs and judgements are frozen for a dataset version:

```powershell
python tools/split_query_set.py `
  --queries benchmarks/research/queries-v2.psv `
  --output-dir benchmarks/research/splits-v2 `
  --seed 20260822 `
  --corpus-version corpus-v2 `
  --code-revision <commit>
```

The generated manifest records every query assignment. Development, validation, and test/held-out queries must be disjoint. Do not use the test split for weight tuning, model selection, query rewriting, or error-driven corpus changes.

The research split cannot be created until candidate review and human qrels release are complete. See [split freeze](../../docs/research/split-freeze.md).
