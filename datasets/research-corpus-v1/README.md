# NEBULA research-corpus-v1

This directory is the first public engineering corpus snapshot for NEBULA. It is separate from `benchmarks/evaluation/corpus-v1`, which remains the small synthetic regression fixture.

## Status

**PROVISIONAL - NEEDS HUMAN LICENSING REVIEW**

The source-level licenses are recorded in `benchmarks/research/source-registry-v1.psv`, but repository-level licensing does not automatically resolve third-party excerpts, generated content, attribution requirements, or redistribution obligations for every document. Do not describe this snapshot as legally cleared until a human review is recorded.

## Layout

- `raw/` — exact upstream source bytes selected by the deterministic acquisition tool.
- `normalized/` — UTF-8 normalized retrieval text used by downstream indexing.
- `manifests/research-corpus-v1.json` — document-level provenance and checksums.
- `manifests/rejected-sources.json` — files excluded during acquisition and the reason.
- `licenses/` — retained upstream license/notice files when acquired.
- `metadata/` — generated statistics and attribution summaries.

The snapshot records upstream repository, commit, source URL, retrieval time, raw checksum, normalized checksum, version, transformation notes, and word count for every admitted document. No private, authenticated, paywalled, user-data, or unclear-license source is permitted.

The current candidate query pool is `metadata/candidate-queries.psv` with its review sheet and distribution report. All 400 candidates are `NEEDS_HUMAN_REVIEW`; the pool contains no relevance judgements and no final held-out split.

The automated query-quality audit is written to reports/generated/query-quality-audit.json, query-quality-audit.csv, and query-quality-audit.md; it flags mechanical templates for human inspection and never approves queries or creates qrels.

Review batch 01 is versioned under metadata/review-batch-01/. Reviewers should fill reviewer_decision and reviewer_notes in the CSV; this is query approval only, not relevance labelling.
The reviewed-query draft is derived from the completed review sheet under metadata/review-batch-01/reviewed-query-draft/. It contains 47 proposed queries (2 approvals and 45 rewrites); 13 rejected candidates are excluded. Two category choices remain marked CATEGORY_REVIEW_REQUIRED. The draft is not benchmark-ready and does not contain qrels.

Normalized-text checksums are defined over canonical LF bytes, so validation is stable across Windows and Unix checkouts; raw-source checksums remain byte-exact.

## Reproduction

The upstream repositories are not vendored into this repository. Reproduce the snapshot from fixed public checkouts:

```powershell
python tools/acquire_research_corpus.py `
  --registry benchmarks/research/source-registry-v1.psv `
  --kubernetes-repo C:\path\to\kubernetes-website `
  --postgresql-repo C:\path\to\postgres `
  --output-root datasets/research-corpus-v1
python tools/validate_research_corpus.py `
  --manifest datasets/research-corpus-v1/manifests/research-corpus-v1.json `
  --corpus-root datasets/research-corpus-v1
```

The exact source commits used for this snapshot are in the manifest. Re-running with a different upstream commit creates a new snapshot and must not silently overwrite a frozen release.
