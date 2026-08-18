# NEBULA Lexical Index

The lexical index is the first search-engine component in NEBULA.

## Current capabilities

- Locale-stable lowercase tokenization
- Unicode letter and number token support
- Positional posting lists
- Term frequency and document frequency
- Document length and average document length statistics
- Duplicate document protection
- Deterministic in-memory indexing

The initial analyzer intentionally does not remove stop words or stem terms. Those policies will be evaluated against a labelled query set rather than introduced without evidence.
