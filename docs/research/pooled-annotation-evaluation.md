# Pooled annotation evaluation

## Purpose

The current protected batch labels one grounding page and three seeded random
same-project distractors per query. That packet is useful for process testing,
but it cannot support broad agreement or corpus-wide relevance claims.

The next evaluation wave uses a depth-10 pooled design. It is a new
pre-registered evaluation, not a re-run of the already-spent 8-query held-out
test. For the current development wave, use only development and validation
queries; seal a fresh held-out query file before any future test evaluation.
For each approved query,
the private candidate artifact takes the union of the top 10 results from each
pre-registered retrieval mode. Duplicate documents are retained once and record
which systems retrieved them and at which ranks. The grounding document is not
injected; it is included only if a system retrieves it naturally.

The current implementation is `com.nebula.evaluation.AnnotationCandidateRetriever`.
For example:

```powershell
java -cp build/classes com.nebula.evaluation.AnnotationCandidateRetriever `
  <approved-queries.psv> `
  <corpus-manifest.json> `
  <private-output-dir> `
  10 `
  bm25,semantic,hybrid,bge-hybrid,a3_hybrid_freshness,a4_hybrid_authority,a5_hybrid_graph,a6_hybrid_freshness_authority,a7_hybrid_authority_graph,a8_hybrid_freshness_graph,a9_full_nebula `
  20261106 `
  <fixed-now-epoch-millis> `
  --embedding-cache <pinned-bge-cache.tsv> `
  --embedding-config experiments/retrieval/modern-embedding-bge-base-en-v1.5.json `
  --split-manifest <frozen-split-manifest.json>
```

For a fresh sealed test set that must be pooled before qrels exist, add
`--annotation-only` and pass a private annotation-pool manifest whose query
assignments are explicitly `test`. This mode is permitted only to build a
blinded candidate pool; it records `annotationOnly=true` and must never be
used as an evaluation split or treated as evidence. The normal mode continues
to reject test-assigned queries and accepts only development/validation
queries.

The `--embedding-cache` and `--embedding-config` arguments are mandatory for
the BGE research pool. Without
it, the retriever uses the 128-dimensional hashing fallback and cannot produce
the pinned BGE results used by the existing held-out comparison. The retriever
requires the exact configured model ID
`BAAI/bge-base-en-v1.5@a5beb1e3e68b9ab74eb54cfd186867f64f240e1a`, records the
cache SHA-256, model ID, and dimension, and the pool must include `bge-hybrid`
plus the BGE-backed A3-A9 variants from the same catalog. Every pool run must
provide `--split-manifest`; the retriever checks that every query is assigned
development or validation and rejects test or unknown IDs. The optional
`--exclude-query-ids` file adds another deny-list check. Freshness-using
modes must provide an explicit fixed timestamp; the tool never silently uses
wall-clock time for those modes.

The output is private because it contains retrieval ranks, scores, system
membership, source paths, and query-to-document mappings. Pass its candidate
file to `tools/generate_blinded_annotation_packages.py`; the generated
annotator packages contain only query text, document excerpts, and blank label
fields. Ranking metadata and system identity are excluded from those packages.

The earlier local 964-pair artifact was generated before cache enforcement and
used hashing-based retrieval. It is **UNUSABLE FOR ANNOTATION OR RESEARCH
CLAIMS** and must be discarded, not relabelled or merged. Its exact query
contents and scope cannot be independently verified from this public checkout.

## Freeze and release requirements

Before annotation, record the exact query source, corpus manifest, retrieval
modes, top-k depth, model/cache revision, seed, fixed timestamp, and output
checksums. Annotators must judge the pooled documents independently without
seeing retrieval metadata or another annotator's labels. Agreement must be
reported over the full pooled set, including disagreements; adjudication must
precede qrels release.

This tooling creates an annotation artifact, not research evidence. No A3-A9
real-data result may be reported until the pooled labels are independently
completed, validated, adjudicated, and used in a frozen held-out evaluation.

## Interpretation boundary

The pooled design improves coverage of documents surfaced by the evaluated
systems, but it is still a pooled-judgment design rather than exhaustive
corpus annotation. Unpooled documents remain unjudged and must not be treated
as confirmed negatives. Report pool size, per-query coverage, agreement,
judged-positive counts, and the exact system pool alongside any metrics.

Trust-oriented evaluation must also use a provenance-tracked trust metadata
file, a resolved internal graph derived from the corpus manifest, and an
explicit `--now-epoch-millis` value. The graph derivation is described in
[corpus graph provenance](corpus-graph-provenance.md); unresolved links are
left out rather than guessed.
