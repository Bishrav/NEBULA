# Blinded annotation workflow

**Status: HUMAN ANNOTATION REQUIRED**

NEBULA does not generate human labels. The packet generator only creates blinded candidate-document packets and empty annotation templates.

## Annotator procedure

1. Receive only your anonymous packet (`packet-A01.json` or `packet-A02.json`) and the rubric below.
2. Read the query and each candidate document excerpt independently.
3. Assign one grade to every candidate:
   - `0` — irrelevant
   - `1` — useful context
   - `2` — substantially relevant
   - `3` — directly answers the query
4. Mark uncertainty `true` when the evidence is ambiguous or incomplete.
5. Write a short evidence note explaining the decision.
6. Record an ISO-8601 timestamp.
7. Return only the completed anonymous PSV template. Do not add names, emails, rankings, scores, trust values, or other annotators' labels.

Candidate ordering is randomized deterministically from the recorded seed, annotator code, and query ID. The packet exposes no model score or retrieval-system identity.

## Release process

Run `tools/validate_blinded_annotations.py` separately for every annotator. Run `tools/analyze_blinded_agreement.py` to calculate nominal, linear weighted, and quadratic weighted Cohen's kappa and produce a disagreement list. A qualified adjudicator must complete a separate adjudication packet without overwriting original exports.

`tools/build_final_qrels.py` refuses to create qrels unless the release artifact has `status=release-ready`, at least two annotators, an agreement report, and resolved adjudications. Participant identity must remain outside the repository; raw annotation exports must not be committed.

No current human annotation data exist in this repository.
