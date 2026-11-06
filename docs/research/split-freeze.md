# Research split freeze

**Status: COMPLETE FOR PROTECTED REVIEW BATCH - BROADER EVALUATION PENDING**

`tools/freeze_research_split.py` is the only supported path for creating the final research split. It requires:

- human-approved query records with `review_status=APPROVED`;
- qrels covering every approved query;
- a corpus version and qrels version;
- a seed and Git revision;
- an empty output directory.

The splitter stratifies by category, project, difficulty, and freshness sensitivity using a deterministic 60/20/20 round-robin allocation. It writes development, validation, and test query files plus an immutable manifest containing source checksums, qrels checksum, assignments, and the required held-out flag.

Review batch 01 now has a protected frozen split for 47 public approved query texts: 30 development, 9 validation, and 8 test queries. The qrels and split manifest remain in protected study storage. The split is valid for the current protected known-item run; a broader corpus-wide evaluation with more complete judgments remains pending.

## Held-out safety guard

`EvaluationRunner` refuses query files whose names contain `heldout` or `test` unless the command includes:

```text
--final-heldout-evaluation
```

This flag authorizes execution only; it does not replace the frozen-manifest gate or permit tuning after test results are observed.
