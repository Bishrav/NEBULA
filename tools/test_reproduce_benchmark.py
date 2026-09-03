#!/usr/bin/env python3
"""Regression checks for the frozen benchmark reproducer."""

import json
import unittest
from pathlib import Path

import reproduce_benchmark


class ReproduceBenchmarkTest(unittest.TestCase):
    def test_frozen_inputs_match_lock(self) -> None:
        lock = json.loads(reproduce_benchmark.LOCK.read_text(encoding="utf-8"))
        reproduce_benchmark.verify_lock(lock)
        self.assertEqual(lock["benchmarkVersion"], "regression-v1")
        self.assertEqual(lock["corpus"]["documents"], 4)
        self.assertEqual(lock["queries"]["all"]["count"], 30)
        self.assertEqual(lock["queries"]["heldout"]["count"], 10)

    def test_lock_is_in_repository(self) -> None:
        self.assertTrue(Path(reproduce_benchmark.LOCK).is_file())


if __name__ == "__main__":
    unittest.main()
