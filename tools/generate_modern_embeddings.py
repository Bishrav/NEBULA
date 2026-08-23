#!/usr/bin/env python3
"""Generate an offline cache with a pinned SentenceTransformers model.

The package/model download is intentionally optional and is not run in CI. The output
format is consumed by CachedEmbeddingModel without coupling Java ranking code to a
particular inference provider.
"""

import argparse
import hashlib
import json
import sys
from pathlib import Path


MODEL_ID = "BAAI/bge-base-en-v1.5"
MODEL_REVISION = "a5beb1e3e68b9ab74eb54cfd186867f64f240e1a"
MODEL_LICENSE = "MIT"


def text_hash(text): return hashlib.sha256(text.encode("utf-8")).hexdigest()


def load_texts(path):
    rows = [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]
    texts = []
    for number, row in enumerate(rows, 1):
        if not isinstance(row, dict) or not isinstance(row.get("text"), str) or not row["text"].strip():
            raise ValueError(f"input row {number} must contain non-empty text")
        texts.append(row["text"])
    if not texts:
        raise ValueError("input JSONL contains no texts")
    hashes = [text_hash(text) for text in texts]
    if len(hashes) != len(set(hashes)):
        raise ValueError("input JSONL contains duplicate texts")
    return texts


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--texts-jsonl", type=Path, required=True, help="JSONL rows containing a text field")
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--batch-size", type=int, default=32)
    parser.add_argument("--model-id", default=MODEL_ID)
    parser.add_argument("--revision", default=MODEL_REVISION)
    args = parser.parse_args()
    if args.batch_size <= 0: raise ValueError("batch size must be positive")
    if args.model_id != MODEL_ID or args.revision != MODEL_REVISION:
        raise ValueError("only the pinned BGE model ID and revision are accepted; update metadata and tests before changing them")
    try:
        from sentence_transformers import SentenceTransformer
    except ImportError:
        print("ERROR: install the pinned optional dependencies from tools/modern-embeddings-requirements.txt", file=sys.stderr)
        return 1
    texts = load_texts(args.texts_jsonl)
    model = SentenceTransformer(args.model_id, revision=args.revision)
    vectors = model.encode(texts, batch_size=args.batch_size, normalize_embeddings=True, convert_to_numpy=True, show_progress_bar=True)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", encoding="utf-8", newline="\n") as handle:
        handle.write("# schemaVersion=modern-embedding-cache-v1\n# modelId=" + args.model_id + "\n# revision=" + args.revision + "\n# license=" + MODEL_LICENSE + "\n# dimension=" + str(vectors.shape[1]) + "\n# normalized=true\n")
        for text, vector in zip(texts, vectors): handle.write(text_hash(text) + "\t" + ",".join(f"{float(value):.9g}" for value in vector) + "\n")
    print(json.dumps({"status": "created", "modelId": args.model_id, "revision": args.revision, "license": MODEL_LICENSE, "dimension": int(vectors.shape[1]), "vectors": len(texts), "normalized": True}, sort_keys=True))
    return 0


if __name__ == "__main__": raise SystemExit(main())
