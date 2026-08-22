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
    return True, f"{len(rows)} relevance judgements present"


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

    if split.is_file():
        data = json.loads(split.read_text(encoding="utf-8"))
        split_ok = data.get("frozen") is True and data.get("heldoutFlagRequired") == "--final-heldout-evaluation"
        results.append(check("frozen development/validation/test split", split_ok, str(data),
                             "split manifest must be immutable and require the explicit held-out flag"))
    else:
        results.append(check("frozen development/validation/test split", False, "split manifest absent",
                             "create the split only after approved queries and released qrels exist"))

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
