#!/usr/bin/env python3
"""Run a versioned NEBULA Java experiment from a JSON configuration."""

import argparse
import json
import subprocess
import sys
from pathlib import Path


REQUIRED = ("corpus", "queries", "trust", "output", "comparison")


def inside(root, path):
    resolved = (root / path).resolve()
    try:
        resolved.relative_to(root.resolve())
    except ValueError as exc:
        raise ValueError(f"path escapes repository: {path}") from exc
    return resolved


def run(root, config_path):
    config = json.loads(config_path.read_text(encoding="utf-8"))
    missing = [key for key in REQUIRED if key not in config]
    if missing:
        raise ValueError("missing config keys: " + ", ".join(missing))
    corpus = inside(root, config["corpus"])
    queries = inside(root, config["queries"])
    trust = inside(root, config["trust"])
    output = inside(root, config["output"])
    comparison = inside(root, config["comparison"])
    for path in (corpus, queries, trust):
        if not path.exists():
            raise ValueError(f"missing experiment input: {path}")
    output.parent.mkdir(parents=True, exist_ok=True)
    command = ["java", "-cp", str(inside(root, config.get("java_classpath", "build/classes"))),
               "com.nebula.evaluation.EvaluationRunner", str(corpus), str(queries),
               str(trust), str(output), str(comparison)]
    print("RUN:", " ".join(command))
    return subprocess.run(command, cwd=root, check=False).returncode


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--config", type=Path, required=True)
    args = parser.parse_args()
    root = Path.cwd().resolve()
    try:
        config = args.config.resolve()
        config.relative_to(root)
        return run(root, config)
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
