# University submission package

This folder contains the editable outreach template for an academic discussion of NEBULA.

## Before sending

1. Replace every bracketed placeholder in `NEBULA-professor-approval-letter-template.docx`.
2. Keep the project status accurate: NEBULA is a research prototype with preliminary synthetic-fixture evidence.
3. Attach the [university review brief](../tu-darmstadt-submission-brief.md), [academic research brief](../academic-research-brief.md), and [demo workflow](../../demo-workflow.md).
4. Include the [claim ledger](../claim-ledger.md) when a reviewer wants a compact view of what is demonstrated and what remains open.
5. Do not describe the project as affiliated with TU Darmstadt, approved by an ethics board, or validated on human participants unless that becomes true through the appropriate process.

## Local verification

```powershell
python -m unittest discover tools -p "test_*.py"
python tools/validate_claim_ledger.py docs/research/claim-ledger.md
python tools/create_professor_approval_letter.py docs/research/submission/NEBULA-professor-approval-letter-template.docx
```

The generated letter is intentionally a template. Personal details, recipient details, and the final research question should be reviewed by the project owner before external use.
