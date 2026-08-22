import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from validate_human_study_package import REQUIRED, validate


class HumanStudyPackageTest(unittest.TestCase):
    def test_repository_package_is_complete_and_gated(self):
        root = Path(__file__).resolve().parents[1] / "docs" / "research" / "human-study"
        self.assertEqual([], validate(root))
        self.assertGreaterEqual(len(REQUIRED), 10)


if __name__ == "__main__":
    unittest.main()
