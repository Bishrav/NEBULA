#!/usr/bin/env python3
import json
import tempfile
import unittest
from pathlib import Path

from run_experiment import run


class RunnerValidationTest(unittest.TestCase):
    def test_rejects_path_escape(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            config = root / "config.json"
            config.write_text(json.dumps({"corpus": "../outside"}), encoding="utf-8")
            with self.assertRaises(ValueError):
                run(root, config)


if __name__ == "__main__":
    unittest.main()
