# Modern embedding baseline

NEBULA retains the hashing and character n-gram embeddings as transparent, dependency-free baselines. The modern baseline is `BAAI/bge-base-en-v1.5`, pinned to revision `a5beb1e3e68b9ab74eb54cfd186867f64f240e1a`.

The official [model card](https://huggingface.co/BAAI/bge-base-en-v1.5) describes it as an English retrieval embedding model, reports 768-dimensional vectors, and identifies the model license as MIT. The repository records this as model metadata, not as a claim that the model is superior for NEBULA's corpus.

## Reproducible generation

Install the optional pinned environment, prepare JSONL rows with a `text` field, and generate an offline cache:

```powershell
python -m pip install -r tools/modern-embeddings-requirements.txt
python tools/generate_modern_embeddings.py `
  --texts-jsonl .\experiments\retrieval\modern-inputs.jsonl `
  --output .\experiments\retrieval\bge-base-en-v1.5.cache.tsv `
  --batch-size 32
```

Build unique, deterministic inputs first:

```powershell
python tools/build_modern_embedding_inputs.py `
  --corpus .\benchmarks\evaluation\corpus-v1 `
  --queries .\benchmarks\evaluation\queries-v1.psv `
  --output .\reports\generated\modern-inputs.jsonl `
  --manifest .\reports\generated\modern-inputs-manifest.json
```

The current environment does not have the pinned optional inference packages,
so BGE generation is **ENVIRONMENT-LIMITED / NOT YET MEASURED** here. Install
the pinned requirements and run the command only when the model revision and
cache checksum can be recorded.

The generator pins the model revision, batches inference, L2-normalizes vectors,
rejects empty or duplicate input texts, and records metadata in the cache
header. The cache loader rejects duplicate keys, non-finite values, dimension
mismatches, and non-unit vectors when normalization is declared. It fails
explicitly if requested text is absent rather than silently substituting another
embedding.

## Evaluation protocol

Once human-reviewed queries and frozen qrels exist, compare:

- hashing semantic-only and hybrid;
- character n-gram semantic-only and hybrid;
- BGE semantic-only and weighted hybrid;
- BM25+dense Reciprocal Rank Fusion;
- full NEBULA with the frozen dense baseline.

Record model revision, license, dimension, normalization, batch size, cache checksum, indexing time, embedding generation time, query latency, NDCG@k, MRR, and Recall@k. Current quality and latency results are **NOT YET MEASURED**; no improvement claim is permitted.
