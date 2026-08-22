# Frozen blinded annotation pipeline

NEBULA's human relevance-judgement stage uses a frozen corpus and frozen query
wording before any labels are collected.

## Frozen inputs

- Query source: `annotation-query-source.psv`, containing 379 rewritten queries.
- Corpus: `datasets/research-corpus-v1`, admitted through its versioned manifest.
- Fixed development retrieval configuration: hybrid lexical `0.50` plus semantic
  `0.50`, with the retrieval timestamp and seed recorded in a private manifest.
- Candidate depth: top 10 per query for the current annotation snapshot.

The retriever reads only corpus-manifest documents. It does not inject the
original grounding document and does not manually add documents that appear
useful.

## Private retrieval artifact

The private artifact records query/document pairs, retrieval rank, title,
excerpt, score, source path, source URL, corpus checksum, configuration, seed,
and the mapping from anonymous annotation IDs to original query IDs. It must
not be distributed to annotators.

## Sanitized annotator packages

Two packages are generated from the same private candidate pairs. Each package
contains query text, document ID, title, excerpt, and empty fields for grade
0–3, uncertainty, evidence note, and timestamp. Candidate order is shuffled
with a separate deterministic seed per annotator and query. Neither package
contains ranks, scores, trust signals, source URLs, original grounding IDs, or
system identity.

Run the generator with:

```powershell
python tools/generate_blinded_annotation_packages.py `
  --candidates reports/generated/annotation-retrieval-private/retrieval-candidates.psv `
  --queries C:\path\to\annotation-query-source.psv `
  --private-dir reports/generated/annotation-private `
  --output-dir reports/generated/annotation-packages `
  --seed 20260822 --packet-size 25
```

The generated ZIP files are local research artifacts and are intentionally not
committed to the repository. Human annotation remains required; no labels,
agreement values, or qrels are claimed by this pipeline.

## Required annotation process

Provide one package to each independent annotator. Annotators assign the rubric
grade, uncertainty flag, evidence note, and timestamp without seeing the other
annotator's labels. After both assignments are returned, run validation,
agreement analysis, disagreement adjudication, and the guarded final-qrels
builder. Original annotations must remain immutable.

Validate a returned ZIP or unpacked directory and create the protected
canonical export used by agreement tooling:

```powershell
python tools/validate_annotation_submission.py `
  --submission C:\protected\annotator-A-completed.zip `
  --packet reports/generated/annotation-packages/blinded-annotation-package-A.zip `
  --private-key reports/generated/annotation-private/PRIVATE-query-id-key.psv `
  --annotator A `
  --output C:\protected\annotation-A.canonical.psv
```

The private key is used only to normalize anonymous query IDs for protected
analysis. Keep it and canonical exports in protected study storage; never
commit them or send them to annotators.
