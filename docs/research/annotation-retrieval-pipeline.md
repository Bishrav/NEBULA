# Frozen blinded annotation pipeline

NEBULA's human relevance-judgement stage uses a frozen corpus and frozen query
wording before any labels are collected.

## Frozen inputs

- Query source: the versioned query source `datasets/research-corpus-v1/metadata/review-batch-01/reviewed-query-draft/reviewed-query-draft.psv` containing 47 finalized reviewed queries in this checkout.
- Corpus: `datasets/research-corpus-v1`, admitted through its versioned manifest.
- Annotation packet: one grounded corpus document plus three deterministic
  same-project distractors per query for this review batch. The builder shuffles
  the distractor pool with a seeded pseudorandom generator and then shuffles the
  four selected candidates. This packet is for annotation only; it is not
  the search evaluator's candidate set.

This script does **not** run a retrieval system. The private file's legacy
`retrieval_rank` column records the shuffled candidate position, not a system
rank, and its `score` field is blank. The candidate artifact therefore records
annotation-packet provenance, not retrieval quality.

## Private candidate-pool artifact

The private artifact records query/document pairs, candidate position, title,
excerpt, source path, source URL, corpus checksum, candidate-construction seed,
and the mapping from anonymous annotation IDs to original query IDs. It must
not be distributed to annotators.

## Interpretation limitation

Each query has only four labelled documents: the grounding page and three
random same-project distractors. The other corpus documents are unjudged, so a
zero grade in this packet is not evidence that every unshown document is
irrelevant. Agreement measures consistency on this constrained annotation task;
the observed kappa of 1.0 should not be presented as evidence that the rubric
has established broad relevance-label quality. Results from these qrels measure
known-item/source-page retrieval against incomplete judgments. The evaluator
ranks all indexed corpus documents and scores only the protected qrel entries;
the four-document packet is the judged subset, not the search candidate set, and
unjudged documents are not established negatives.

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
  --queries C:\path\to\datasets/research-corpus-v1/metadata/review-batch-01/reviewed-query-draft/reviewed-query-draft.psv `
  --private-dir reports/generated/annotation-private `
  --output-dir reports/generated/annotation-packages `
  --seed 20260822 --packet-size 25
```

The generated ZIP files and completed canonical exports are local research
artifacts and are intentionally not committed to the repository. The protected
review batch has completed validation, agreement, consensus release, and qrels
generation; the public repository records the process and evidence boundary,
not the protected labels.

## Next pooled evaluation wave

The current four-document packet is preserved for reproducibility. The next
wave should use the pooled design in
[pooled-annotation-evaluation.md](pooled-annotation-evaluation.md): top-10
results from each pre-registered system are unioned before blinded annotation.
The Java retriever records private system/rank provenance, while the package
generator strips that metadata from annotator-facing files. This is new
annotation infrastructure; it does not change the current protected results or
create A3-A9 evidence until humans complete the pooled labels and release gate.

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
