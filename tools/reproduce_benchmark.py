#!/usr/bin/env python3
"""Reproduce the frozen synthetic NEBULA retrieval benchmark."""

import hashlib
import json
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
LOCK = ROOT / "experiments" / "retrieval" / "regression-v1.lock.json"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def verify_lock(lock: dict[str, object]) -> None:
    mismatches: list[str] = []

    def verify(path: Path, expected: str) -> None:
        actual = sha256(path) if path.is_file() else None
        if actual != expected:
            mismatches.append(
                f"{path.relative_to(ROOT)} (expected {expected}, actual {actual or 'missing'})"
            )

    corpus = lock["corpus"]
    assert isinstance(corpus, dict)
    corpus_files = corpus["files"]
    assert isinstance(corpus_files, dict)
    for relative, expected in corpus_files.items():
        assert isinstance(expected, str)
        verify(ROOT / relative, expected)

    queries = lock["queries"]
    assert isinstance(queries, dict)
    for section in ("all", "train", "heldout"):
        entry = queries[section]
        assert isinstance(entry, dict)
        verify(ROOT / entry["path"], entry["sha256"])

    trust = lock["trust"]
    assert isinstance(trust, dict)
    verify(ROOT / trust["path"], trust["sha256"])

    if mismatches:
        raise RuntimeError("frozen benchmark inputs changed: " + "; ".join(mismatches))


def run(args: list[str]) -> None:
    subprocess.run(args, cwd=ROOT, check=True)


def main() -> int:
    lock = json.loads(LOCK.read_text(encoding="utf-8"))
    verify_lock(lock)
    classes = ROOT / "build" / "classes"
    classes.mkdir(parents=True, exist_ok=True)
    sources = sorted(
        path
        for source_root in (
            ROOT / "services" / "ingestion" / "src" / "main" / "java",
            ROOT / "algorithms" / "lexical" / "src" / "main" / "java",
        )
        for path in source_root.rglob("*.java")
    )
    run(["javac", "-encoding", "UTF-8", "-d", str(classes), *(str(path) for path in sources)])
    output = ROOT / "reports" / "generated" / "regression-v1"
    output.mkdir(parents=True, exist_ok=True)
    base = [
        "java",
        "-cp",
        str(classes),
        "com.nebula.evaluation.EvaluationRunner",
        "benchmarks/evaluation/corpus-v1",
    ]
    for name, extra in (
        ("all", ["benchmarks/evaluation/queries-v1.psv"]),
        ("train", ["benchmarks/evaluation/queries-v1-train.psv"]),
        ("heldout", ["benchmarks/evaluation/queries-v1-heldout.psv", "--final-heldout-evaluation"]),
    ):
        run(
            base
            + extra[:1]
            + [
                "benchmarks/evaluation/trust-v1.psv",
                str(output / f"{name}.md"),
                str(output / f"{name}.json"),
            ]
            + extra[1:]
        )
    print(f"Reproduced frozen benchmark: {output}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (AssertionError, KeyError, OSError, RuntimeError, subprocess.CalledProcessError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        raise SystemExit(1) from error
