import tempfile
import unittest
from pathlib import Path

from validate_claim_ledger import validate


HEADER = "| Claim | Evidence required | Status | Safe wording now |\n| --- | --- | --- | --- |\n"


class ClaimLedgerTest(unittest.TestCase):
    def test_valid_ledger(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "claims.md"
            path.write_text(HEADER + "| A claim | A test | NOT YET MEASURED | The system exposes a signal. |\n", encoding="utf-8")
            self.assertEqual(1, validate(path)["claimCount"])

    def test_duplicate_claim_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "claims.md"
            path.write_text(HEADER + "| Same | Test | NOT YET MEASURED | Safe. |\n| same | Test | NOT YET MEASURED | Safe. |\n", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "duplicate claim"):
                validate(path)

    def test_unsupported_status_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "claims.md"
            path.write_text(HEADER + "| A claim | A test | PROVEN | Safe. |\n", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "unsupported claim status"):
                validate(path)


if __name__ == "__main__":
    unittest.main()
