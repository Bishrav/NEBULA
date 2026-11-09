# Authority metadata provenance

Source authority is an observable ranking input, not a truth label. A
research-grade authority value must have a documented category definition,
source provenance, reviewer or ownership record, and a frozen checksum.

The current `research-corpus-v1` manifest contains one authority category:
`official-project-documentation`, for all 843 Kubernetes and PostgreSQL
documents. The current private derived metadata therefore uses a uniform
authority value and must not support a claim that authority variation improves
retrieval.

Before an authority ablation is treated as evidence, a future metadata wave
must either admit sources with independently justified authority categories or
obtain an independent review that defines and validates source-level authority
differences without using relevance labels or retrieval outcomes.

The review record must include the category rubric, source evidence, reviewer,
timestamp, manifest checksum, missingness report, and a manipulation/bias
check. Until that exists, A4, A6, A7, and the authority component of A9 remain
diagnostic only.

`tools/build_authority_review_template.py` creates the blank, checksum-linked
review file for this process; it never assigns authority values automatically.
