#!/usr/bin/env python3
"""Create or verify a checksum-backed NEBULA experiment manifest."""

import argparse
import hashlib
import json
import os
import platform
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path


def sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def git_revision(root):
    try:
        return subprocess.check_output(["git", "-C", str(root), "rev-parse", "HEAD"], text=True).strip()
    except (OSError, subprocess.CalledProcessError):
        return "unknown"


def git_dirty(root):
    try:
        result = subprocess.check_output(["git", "-C", str(root), "status", "--porcelain"], text=True)
        return bool(result.strip())
    except (OSError, subprocess.CalledProcessError):
        return None


def runtime_metadata():
    try:
        java = subprocess.check_output(["java", "-version"], text=True, stderr=subprocess.STDOUT).strip()
    except (OSError, subprocess.CalledProcessError):
        java = "UNAVAILABLE"
    return {
        "python": sys.version,
        "java": java,
        "os": platform.platform(),
        "architecture": platform.machine(),
        "processor": platform.processor() or "UNAVAILABLE",
        "cpuCount": os.cpu_count(),
    }


def build_manifest(root, files, generated_at):
    entries = {}
    for label, path in files.items():
        resolved = path.resolve()
        candidates = sorted(resolved.rglob("*") if resolved.is_dir() else [resolved])
        candidates = [candidate for candidate in candidates if candidate.is_file()]
        if not candidates:
            raise ValueError(f"missing experiment input: {path}")
        for candidate in candidates:
            try:
                relative = candidate.relative_to(root.resolve()).as_posix()
            except ValueError:
                relative = str(candidate)
            entry_label = label if len(candidates) == 1 else f"{label}/{candidate.relative_to(resolved).as_posix()}"
            entries[entry_label] = {"path": relative, "sha256": sha256(candidate), "bytes": candidate.stat().st_size}
    return {
        "schemaVersion": "experiment-manifest-v1",
        "generatedAt": generated_at or datetime.now(timezone.utc).replace(microsecond=0).isoformat(),
        "gitRevision": git_revision(root),
        "gitDirtyTree": git_dirty(root),
        "runtime": runtime_metadata(),
        "inputs": entries,
    }


def verify(manifest_path, root):
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    if manifest.get("schemaVersion") != "experiment-manifest-v1":
        raise ValueError("unsupported experiment manifest schema")
    mismatches = []
    for label, entry in manifest.get("inputs", {}).items():
        path = Path(entry["path"])
        if not path.is_absolute(): path = root / path
        if not path.is_file():
            mismatches.append(f"{label}: missing file")
        elif sha256(path) != entry.get("sha256") or path.stat().st_size != entry.get("bytes"):
            mismatches.append(f"{label}: checksum or size changed")
    if mismatches: raise ValueError("; ".join(mismatches))
    return {"status": "verified", "inputs": len(manifest.get("inputs", {}))}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--output", type=Path)
    parser.add_argument("--verify", type=Path)
    parser.add_argument("--corpus", type=Path)
    parser.add_argument("--queries", type=Path)
    parser.add_argument("--query-metadata", type=Path)
    parser.add_argument("--trust", type=Path)
    parser.add_argument("--benchmark", type=Path)
    parser.add_argument("--ann-benchmark", type=Path)
    parser.add_argument("--graph-benchmark", type=Path)
    parser.add_argument("--generated-at")
    args = parser.parse_args()
    try:
        if args.verify:
            print(json.dumps(verify(args.verify, args.root), sort_keys=True))
            return 0
        if not args.output or not args.corpus or not args.queries or not args.trust:
            parser.error("--output, --corpus, --queries, and --trust are required when generating")
        files = {"corpus": args.corpus, "queries": args.queries, "trust": args.trust}
        if args.query_metadata: files["queryMetadata"] = args.query_metadata
        for label, path in (("benchmark", args.benchmark), ("annBenchmark", args.ann_benchmark), ("graphBenchmark", args.graph_benchmark)):
            if path: files[label] = path
        manifest = build_manifest(args.root, files, args.generated_at)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        print(f"GENERATED: {args.output}")
        return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
