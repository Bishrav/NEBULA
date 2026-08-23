"""Check whether NEBULA is allowed to run the frozen held-out evaluation."""

import argparse
import csv
import json
import subprocess
from pathlib import Path


def check(name, passed, evidence, blocker=None):
    return {"name": name, "status": "PASS" if passed else "BLOCKED", "evidence": evidence, "blocker": blocker}


def git_clean(root):
    result = subprocess.run(["git", "status", "--porcelain"], cwd=root, capture_output=True, text=True)
    if result.returncode != 0:
        return False
    return not result.stdout.strip()


def approved_queries(path):
    if not path.is_file():
        return False, "approved query file is absent"
    with path.open(encoding="utf-8", newline="") as handle:
        rows = list(csv.DictReader(line for line in handle if not line.startswith("#")))
    if not rows:
        return False, "approved query file is empty"
    required = {"query_id", "query_text", "review_status"}
    if not required.issubset(rows[0]):
        return False, "approved query file is missing required columns"
    ids = [row.get("query_id", "") for row in rows]
    if any(not value for value in ids) or len(ids) != len(set(ids)):
        return False, "approved query IDs must be present and unique"
    statuses = {row.get("review_status", "") for row in rows}
    if statuses != {"APPROVED"}:
        return False, f"query statuses are {sorted(statuses)}; every query must be APPROVED"
    return True, f"{len(rows)} approved queries"


def qrels_ready(path):
    if not path.is_file():
        return False, "human qrels file is absent"
    with path.open(encoding="utf-8", newline="") as handle:
        rows = list(csv.DictReader(line for line in handle if not line.startswith("#")))
    if not rows:
        return False, "qrels file is empty"
    if set(rows[0]) != {"query_id", "document_id", "relevance_grade"}:
        return False, "qrels schema is incomplete"
    pairs = set()
    for row in rows:
        pair = (row.get("query_id", ""), row.get("document_id", ""))
        if not pair[0] or not pair[1] or pair in pairs:
            return False, "qrels query/document pairs must be present and unique"
        try:
            grade = int(row.get("relevance_grade", ""))
        except ValueError:
            return False, "qrels relevance grades must be integers from 0 to 3"
        if grade not in range(4):
            return False, "qrels relevance grades must be integers from 0 to 3"
        pairs.add(pair)
    release_path = path.with_suffix(path.suffix + ".release.json")
    if not release_path.is_file():
        return False, "qrels release sidecar is absent"
    try:
        release = json.loads(release_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        return False, "qrels release sidecar is invalid JSON"
    required_release = release.get("status") == "release-ready" and release.get("annotatorCount", 0) >= 2 and release.get("agreementComputed") is True and release.get("adjudicationComplete") is True
    if not required_release:
        return False, "qrels release requires two annotators, agreement, and completed adjudication"
    return True, f"{len(rows)} relevance judgements; release sidecar verified"


def split_ready(path):
    if not path.is_file():
        return False, "split manifest absent"
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        return False, "split manifest is invalid JSON"
    required = {"development", "validation", "test"}
    if data.get("frozen") is not True or data.get("heldoutFlagRequired") != "--final-heldout-evaluation" or not required.issubset(data):
        return False, "split must be frozen, contain development/validation/test, and require the held-out flag"
    sets = []
    for name in sorted(required):
        ids = data[name].get("queryIds", []) if isinstance(data[name], dict) else []
        if not ids or len(ids) != len(set(ids)):
            return False, f"{name} query IDs must be present and unique"
        sets.append(set(ids))
    if set.union(*sets) and any(left & right for index, left in enumerate(sets) for right in sets[index + 1:]):
        return False, "development, validation, and test query IDs must be disjoint"
    return True, "frozen disjoint development/validation/test query sets"


def evaluate(root, corpus, queries, qrels, split, ranking, embedding, statistics):
    results = []
    corpus_stats_path = corpus.parent.parent / "metadata" / "corpus-statistics.json"
    if corpus.is_file():
        manifest = json.loads(corpus.read_text(encoding="utf-8"))
        stats = json.loads(corpus_stats_path.read_text(encoding="utf-8")) if corpus_stats_path.is_file() else {}
        count = len(manifest.get("documents", []))
        provisional = bool(stats.get("provisional", True))
        results.append(check("frozen licensed corpus", count >= 500 and not provisional,
                             f"{count} documents; provisional={provisional}",
                             "corpus must be admitted after licensing review and contain at least 500 documents"))
    else:
        results.append(check("frozen licensed corpus", False, "manifest absent", "corpus manifest is required"))

    query_ok, query_evidence = approved_queries(queries)
    results.append(check("approved research queries", query_ok, query_evidence, "human review must approve every query"))
    qrels_ok, qrels_evidence = qrels_ready(qrels)
    results.append(check("human qrels and agreement release", qrels_ok, qrels_evidence,
                         "independent annotations, agreement, adjudication, and qrels release are required"))

    split_ok, split_evidence = split_ready(split)
    results.append(check("frozen development/validation/test split", split_ok, split_evidence,
                         "split manifest must be immutable, complete, and disjoint"))

    if ranking.is_file():
        data = json.loads(ranking.read_text(encoding="utf-8"))
        dev_only = "development" in data.get("purpose", "").lower()
        results.append(check("frozen ranking configuration", not dev_only, data.get("purpose", "unspecified"),
                             "freeze a final configuration separate from development tuning"))
    else:
        results.append(check("frozen ranking configuration", False, "ranking configuration absent",
                             "version the final ranking configuration"))

    if embedding.is_file():
        data = json.loads(embedding.read_text(encoding="utf-8"))
        status = data.get("evidenceStatus", "")
        ready = "NOT YET" not in status.upper()
        results.append(check("frozen embedding model", ready, status,
                             "choose and freeze the model only after validation-set decisions"))
    else:
        results.append(check("frozen embedding model", False, "embedding configuration absent",
                             "version the final embedding model"))

    results.append(check("statistical analysis protocol", statistics.is_file(), str(statistics),
                         "statistical analysis protocol is required"))
    results.append(check("clean Git working tree", git_clean(root), "git status --porcelain",
                         "commit or discard all changes before final evaluation"))
    return {"schemaVersion": "final-evaluation-gate-v1", "overall": "READY" if all(item["status"] == "PASS" for item in results) else "BLOCKED", "checks": results}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    root = args.root.resolve()
    result = evaluate(
        root,
        root / "datasets/research-corpus-v1/manifests/research-corpus-v1.json",
        root / "datasets/research-corpus-v1/metadata/approved-queries.psv",
        root / "datasets/research-corpus-v1/metadata/qrels-v1.psv",
        root / "datasets/research-corpus-v1/splits/split-manifest.json",
        root / "experiments/retrieval/final-ranking.json",
        root / "experiments/retrieval/final-embedding.json",
        root / "docs/research/statistical-analysis.md",
    )
    text = json.dumps(result, indent=2) + "\n"
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(text, encoding="utf-8")
    print(text, end="")
    if result["overall"] != "READY":
        raise SystemExit(1)


if __name__ == "__main__":
    main()
