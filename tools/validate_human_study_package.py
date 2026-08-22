"""Validate that the human-study draft package is present and clearly gated."""

from pathlib import Path


MARKER = "DRAFT — REQUIRES INSTITUTIONAL/SUPERVISOR REVIEW"
REQUIRED = (
    "README.md",
    "protocol.md",
    "participant-information-sheet.md",
    "consent-form.md",
    "data-management-plan.md",
    "risk-assessment.md",
    "task-script.md",
    "questionnaire.md",
    "debriefing.md",
    "analysis-plan.md",
    "submission-checklist.md",
)


def validate(root):
    errors = []
    for name in REQUIRED:
        path = root / name
        if not path.is_file():
            errors.append(f"missing package document: {name}")
            continue
        content = path.read_text(encoding="utf-8")
        if MARKER not in content:
            errors.append(f"missing review marker: {name}")
    if "No participant recruitment" not in (root / "README.md").read_text(encoding="utf-8"):
        errors.append("README must prohibit recruitment")
    return errors


def main():
    package = Path(__file__).resolve().parents[1] / "docs" / "research" / "human-study"
    errors = validate(package)
    if errors:
        raise SystemExit("\n".join(errors))
    print(f"VALID: {package}")


if __name__ == "__main__":
    main()
