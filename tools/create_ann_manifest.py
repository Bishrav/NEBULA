"""Create a reproducibility manifest for an ANN benchmark artifact."""

import argparse
import hashlib
import json
import platform
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path


def command(*args):
    try:
        return subprocess.check_output(args, text=True, stderr=subprocess.STDOUT).strip()
    except (OSError, subprocess.CalledProcessError):
        return "UNAVAILABLE"


def sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for block in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def build_manifest(benchmark_path, status):
    return {
        "schemaVersion": "ann-manifest-v1",
        "createdAt": datetime.now(timezone.utc).isoformat(),
        "status": status,
        "benchmarkArtifact": str(benchmark_path),
        "benchmarkSha256": sha256(benchmark_path),
        "gitCommit": command("git", "rev-parse", "HEAD"),
        "gitDirtyTree": bool(command("git", "status", "--porcelain")),
        "python": sys.version,
        "java": command("java", "-version"),
        "os": platform.platform(),
        "architecture": platform.machine(),
        "processor": platform.processor() or "UNAVAILABLE",
        "cpuCount": __import__("os").cpu_count(),
        "interpretationBoundary": "Synthetic-vector ANN systems evidence only; not semantic retrieval quality or production capacity.",
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("benchmark", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--status", default="NOT_YET_MEASURED")
    args = parser.parse_args()
    manifest = build_manifest(args.benchmark, args.status)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    print(f"GENERATED: {args.output}")


if __name__ == "__main__":
    main()
