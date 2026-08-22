import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from report_ann_scale import FIELDS, flatten, write_reports


class AnnScaleReportTest(unittest.TestCase):
    def test_flatten_preserves_trial_parameters_and_metrics(self):
        payload = {
            "schemaVersion": "ann-scale-v1",
            "dimension": 128,
            "results": [{"vectors": 10, "M": 8, "efConstruction": 100, "efSearch": 20,
                          "trials": [{"repeat": 0, "recallAt10": 0.9}]}],
        }
        rows = flatten(payload)
        self.assertEqual(1, len(rows))
        self.assertEqual(8, rows[0]["M"])
        self.assertEqual(0.9, rows[0]["recallAt10"])

    def test_reports_are_machine_and_human_readable(self):
        payload = {
            "schemaVersion": "ann-scale-v1", "dimension": 128,
            "results": [{"vectors": 10, "M": 8, "efConstruction": 100, "efSearch": 20,
                          "trials": [{"repeat": 0, "recallAt10": 0.9}]}],
        }
        with tempfile.TemporaryDirectory() as directory:
            csv_path, markdown_path = write_reports(payload, Path(directory))
            self.assertEqual(FIELDS, csv_path.read_text(encoding="utf-8").splitlines()[0].split(","))
            self.assertIn("Synthetic-vector systems benchmark", markdown_path.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
