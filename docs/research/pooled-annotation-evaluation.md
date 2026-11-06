# Pooled annotation evaluation

## Purpose

The current protected batch labels one grounding page and three seeded random
same-project distractors per query. That packet is useful for process testing,
but it cannot support broad agreement or corpus-wide relevance claims.

The next evaluation wave uses a depth-10 pooled design. For each approved query,
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
  bm25,semantic,hybrid,a3_hybrid_freshness,a4_hybrid_authority,a5_hybrid_graph,a6_hybrid_freshness_authority,a7_hybrid_authority_graph,a8_hybrid_freshness_graph,a9_full_nebula `
  20261106 `
  <fixed-now-epoch-millis>
```

The output is private because it contains retrieval ranks, scores, system
membership, source paths, and query-to-document mappings. Pass its candidate
file to `tools/generate_blinded_annotation_packages.py`; the generated
annotator packages contain only query text, document excerpts, and blank label
fields. Ranking metadata and system identity are excluded from those packages.

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
