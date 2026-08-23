#!/usr/bin/env python3
"""Validate the publication claim ledger as a safe, reviewable Markdown table."""

import argparse
import re
import sys
from pathlib import Path

STATUSES = {"COMPLETE", "PARTIAL", "NOT YET MEASURED", "HUMAN STUDY PENDING", "REJECTED CLAIM"}


def cells(line):
    if not line.strip().startswith("|"):
        return []
    return [cell.strip() for cell in line.strip().strip("|").split("|")]


def validate(path):
    lines = Path(path).read_text(encoding="utf-8-sig").splitlines()
    rows = [cells(line) for line in lines if cells(line)]
    if len(rows) < 3:
        raise ValueError("claim ledger must contain a header, separator, and at least one claim")
    if rows[0] != ["Claim", "Evidence required", "Status", "Safe wording now"]:
        raise ValueError("claim ledger header must be Claim | Evidence required | Status | Safe wording now")
    if any(not re.fullmatch(r":?-{3,}:?", cell) for cell in rows[1]):
        raise ValueError("claim ledger separator is malformed")
    claims = []
    seen = set()
    for row in rows[2:]:
        if len(row) != 4:
            raise ValueError(f"claim row must have four cells: {row}")
        claim, evidence, status, safe = row
        if not claim or not evidence or not safe:
            raise ValueError(f"claim row contains an empty cell: {row}")
        if status not in STATUSES:
            raise ValueError(f"unsupported claim status: {status}")
        key = claim.casefold()
        if key in seen:
            raise ValueError(f"duplicate claim: {claim}")
        seen.add(key)
        if status != "COMPLETE" and any(term in safe.casefold() for term in ("proves", "guarantees", "always", "significant improvement")):
            raise ValueError(f"unsafe absolute language for non-complete claim: {claim}")
        claims.append({"claim": claim, "status": status})
    return {"status": "valid", "claimCount": len(claims), "statuses": {status: sum(row["status"] == status for row in claims) for status in sorted(STATUSES)}}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("ledger", type=Path)
    args = parser.parse_args()
    try:
        result = validate(args.ledger)
    except (OSError, ValueError) as exc:
        print(f"INVALID: {exc}", file=sys.stderr)
        return 1
    print(f"VALID: {result['claimCount']} claims")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
