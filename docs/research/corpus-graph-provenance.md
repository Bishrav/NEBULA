# Corpus graph provenance

The admitted research corpus preserves normalized Markdown documents and a
versioned corpus manifest. The acquisition manifest originally left
`graph_links` empty because links were not resolved against the flattened
normalized filenames.

`tools/derive_corpus_graph.py` reconstructs only internal links that resolve
from a normalized document back to another admitted document in the manifest.
It records the corpus-manifest SHA-256 and reports unresolved relative links;
it never guesses a target or treats external URLs as corpus edges.

The derived graph is a private evaluation input. It is not a truth label:
PageRank is only a structural ranking signal. Authority values and freshness
ages remain separate metadata inputs and must retain their own provenance.

For a reproducible evaluation, pass the derived graph to `EvaluationRunner`
with `--graph-links`, together with the pinned trust metadata and an explicit
`--now-epoch-millis` value.
