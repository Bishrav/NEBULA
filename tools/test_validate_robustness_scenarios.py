import json
import tempfile
import unittest
from pathlib import Path

from validate_robustness_scenarios import validate


class RobustnessScenarioTest(unittest.TestCase):
    def test_repository_matrix_is_complete_and_unmeasured(self):
        root = Path(__file__).resolve().parents[1]
        result = validate(root / "experiments/robustness/scenario-matrix.json")
        self.assertEqual(7, result["scenarios"])
        self.assertFalse(result["measured"])

    def test_measured_status_is_rejected(self):
        root = Path(__file__).resolve().parents[1]
        data = json.loads((root / "experiments/robustness/scenario-matrix.json").read_text(encoding="utf-8"))
        data["status"] = "MEASURED"
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "matrix.json"
            path.write_text(json.dumps(data), encoding="utf-8")
            with self.assertRaises(ValueError):
                validate(path)


if __name__ == "__main__":
    unittest.main()
