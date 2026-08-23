#!/usr/bin/env python3
"""Validate the versioned robustness/bias scenario matrix without measuring it."""

import argparse
import json
from pathlib import Path

REQUIRED_IDS = {"R1", "R2", "R3", "R4", "R5", "R6", "R7"}


def validate(path):
    data = json.loads(path.read_text(encoding="utf-8"))
    if data.get("schemaVersion") != "robustness-scenario-matrix-v1":
        raise ValueError("unexpected robustness matrix schema")
    if data.get("status") != "NOT_YET_MEASURED":
        raise ValueError("robustness matrix must remain NOT_YET_MEASURED until experiments run")
    scenarios = data.get("scenarios", [])
    ids = {item.get("id") for item in scenarios}
    if ids != REQUIRED_IDS or len(scenarios) != len(ids):
        raise ValueError("robustness matrix must contain each R1-R7 exactly once")
    for item in scenarios:
        if not item.get("name") or not item.get("signals") or not item.get("question"):
            raise ValueError(f"incomplete scenario: {item.get('id')}")
    if set(data.get("requiredOutputs", [])) != {"ranking-order", "score-contributions", "rank-delta", "configuration", "manifest"}:
        raise ValueError("required robustness outputs are incomplete")
    return {"status": "valid", "scenarios": len(scenarios), "measured": False}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("matrix", type=Path)
    args = parser.parse_args()
    print(json.dumps(validate(args.matrix), sort_keys=True))


if __name__ == "__main__":
    main()
