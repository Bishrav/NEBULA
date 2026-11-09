#!/usr/bin/env python3
"""Derive a provenance-preserving internal document graph from a corpus manifest."""

import argparse
import hashlib
import json
import posixpath
import re
from pathlib import Path
from urllib.parse import urldefrag, urlsplit


MARKDOWN_LINK = re.compile(r"!?\[[^]]*\]\(([^)\s]+)(?:\s+\"[^\"]*\")?\)")


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def clean_target(target):
    target, _ = urldefrag(target)
    return target.split("?", 1)[0]


def candidate_raw_paths(raw_path, target):
    target = clean_target(target)
    if not target or target.startswith(("#", "http://", "https://", "mailto:")):
        return []
    source = posixpath.normpath(raw_path)
    parent = posixpath.dirname(source)
    candidates = [posixpath.normpath(posixpath.join(parent, target))]
    parts = source.split("/")
    if target.startswith("/") and "docs" in parts:
        root = "/".join(parts[: parts.index("docs")])
        candidates.append(posixpath.normpath(posixpath.join(root, target.lstrip("/"))))
    elif "content" in parts:
        root = "/".join(parts[: parts.index("content") + 1])
        candidates.append(posixpath.normpath(posixpath.join(root, target.lstrip("/"))))
    expanded = []
    for candidate in candidates:
        expanded.append(candidate)
        if not posixpath.splitext(candidate)[1]:
            expanded.append(candidate + ".md")
        if candidate.endswith("/"):
            expanded.append(candidate + "_index.md")
    return expanded


def derive(manifest_path, output_path):
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    root = manifest_path.parent.parent
    documents = manifest["documents"]
    by_raw = {item["raw_path"].replace("\\", "/"): item for item in documents}
    graph = []
    unresolved = 0
    matched_edges = 0
    for document in documents:
        raw_path = document["raw_path"].replace("\\", "/")
        text_path = document["text_path"].replace("\\", "/")
        text = (root / text_path).read_text(encoding="utf-8")
        targets = set()
        for raw_target in MARKDOWN_LINK.findall(text):
            for candidate in candidate_raw_paths(raw_path, raw_target):
                target = by_raw.get(candidate)
                if target is not None:
                    targets.add(target["text_path"].replace("\\", "/").removeprefix("normalized/"))
                    break
            else:
                if clean_target(raw_target) and not urlsplit(clean_target(raw_target)).scheme:
                    unresolved += 1
        matched_edges += len(targets)
        graph.append((document["text_path"].replace("\\", "/").removeprefix("normalized/"), sorted(targets)))
    lines = [
        "# NEBULA derived internal corpus graph",
        f"# corpusManifestSha256={sha256(manifest_path)}",
        "# Links are included only when a Markdown target resolves to another admitted corpus document.",
        "source_path|target_paths",
    ]
    lines.extend(f"{source}|{';'.join(targets)}" for source, targets in graph)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return {"documents": len(documents), "matchedEdges": matched_edges, "unresolvedRelativeLinks": unresolved, "manifestSha256": sha256(manifest_path)}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    print(json.dumps(derive(args.manifest, args.output), sort_keys=True))


if __name__ == "__main__":
    main()
