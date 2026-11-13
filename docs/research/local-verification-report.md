# NEBULA local verification report

**Verification date:** 2026-11-05  
**Branch:** `main`  
**Source revision:** `f00f83c` (`feat: add BGE ranking mode to search UI`)  
**Evidence label:** `MEASURED` for the checks listed below

## Purpose

This report records the checks a reviewer can reproduce on a clean local
checkout. It verifies that the prototype builds, starts, serves the browser
workspace, and reproduces the frozen synthetic benchmark. It does not convert
the benchmark into evidence of general search quality or human benefit.

## Verification summary

| Check | Result | Evidence |
| --- | --- | --- |
| Frozen benchmark inputs | PASS | `tools/reproduce_benchmark.py` verified the lock file |
| Java backend compilation | PASS | Reproducer compiled ingestion and lexical sources |
| Frozen benchmark reproduction | PASS | All, train, and held-out reports generated |
| Local API readiness | PASS | `/health/ready` reported `843` documents |
| Browser workspace | PASS | `http://127.0.0.1:5174` returned HTTP 200 |
| BGE hybrid smoke test | PASS | Frozen Kubernetes query returned 5 results and the expected feature-gates source first |
| UI visual inspection | PASS | BGE selector, explanation, scores, and result cards inspected locally |

## Reproduction commands

From the repository root:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tools\start_local_demo.ps1
python tools/reproduce_benchmark.py
```

The benchmark artifacts are written to:

```text
reports/generated/regression-v1/
```

The browser demo is available at `http://127.0.0.1:5174`.

## Measured benchmark snapshot

The reproducer evaluated four synthetic corpus documents at cutoff 5. The
held-out split contains 10 queries. The values below are copied from the
generated JSON report and are included for traceability, not as a claim of
statistical superiority:

| Variant | Held-out Recall@5 | Held-out MRR | Held-out NDCG@5 |
| --- | ---: | ---: | ---: |
| BM25 | 0.650000 | 0.700000 | 0.653604 |
| Semantic | 1.000000 | 0.816667 | 0.823944 |
| Hybrid | 1.000000 | 0.933333 | 0.927047 |
| Trust-aware | 0.650000 | 0.700000 | 0.650980 |

The full per-query results, paired analysis, category analysis, and sensitivity
outputs remain in the generated report directory. The current fixture is too
small to support publication-grade claims.

## BGE smoke test

The opt-in `bge-hybrid` path was started with the pinned private offline cache
and queried with the frozen Kubernetes feature-gates question. The API returned
HTTP 200, five results, and separate lexical, semantic, and hybrid signals. The
first result was the Kubernetes feature-gates reference document.

This verifies configuration and integration. It does not establish that BGE is
better than the other ranking modes on a broad or independently judged corpus.

## Historical protected research-corpus held-out run (8-query packet)

After the protected final-evaluation gate reported `READY`, the frozen test
split was evaluated once with the pinned BGE cache. The public corpus contains
843 documents, while the protected test packet labels 4 candidate documents for
each of 8 held-out queries (32 judged pairs, including 9 positive grades).
The candidate builder selected one grounding page and three seeded random
same-project distractors; it did not run system retrieval.

| Variant | Recall@5 | MRR | NDCG@5 |
| --- | ---: | ---: | ---: |
| BM25 | 0.687500 | 0.750000 | 0.739665 |
| Frozen BGE hybrid | 0.812500 | 0.875000 | 0.864665 |

The BGE hybrid delta versus BM25 was `+0.125000` NDCG@5 on this held-out
split. The evaluator ranks all 843 corpus documents, while the four-document
packet supplies the protected judgments. This measures known-item/source-page
identification against incomplete judgments, not exhaustive relevance across
the corpus. It is a
measured result from a small protected research batch and is not sufficient to
claim statistical significance, general engineering-search quality, or user
benefit.

The paired NDCG@5 analysis used 10,000 bootstrap resamples and 10,000 sign
permutations with seed `20261105`. Across the 8 paired queries, the mean
difference was `+0.125000`, the median difference was `0.000000`, and the win
rate was `12.5%`. The bootstrap 95% interval was `[0.000000, 0.375000]`; the
permutation p-value and Holm-adjusted p-value were both `1.0`. These are
descriptive uncertainty outputs, not evidence of statistical significance.

This is a historical 8-query artifact retained for reproducibility. It is
superseded for current held-out reporting by the fresh 16-query pooled run in
protected local evaluation storage.

## Evidence boundary

The repository can currently demonstrate a working, inspectable, deterministic
retrieval prototype and a reproducible measurement pipeline. It cannot yet
claim corpus-wide relevance, generalisation to engineering organisations,
user-perceived benefit, production-scale reliability, or human-subject
findings. Those claims require broader candidate coverage or exhaustive
relevance assessment, an adequately powered design, and further review.

## Reviewer handoff

1. Run the one-command launcher.
2. Open the browser workspace and inspect the ranking selector.
3. Run the validated BGE query from `docs/demo-workflow.md`.
4. Open a result's evidence preview and verify the source passage.
5. Run `python tools/reproduce_benchmark.py` and inspect the generated reports.

The working tree used to create this report contains the current documentation
and launcher changes. A release commit should be created only after the owner
reviews those changes.
