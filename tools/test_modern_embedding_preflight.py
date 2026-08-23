import tempfile
import unittest
from pathlib import Path

from modern_embedding_preflight import check


class ModernEmbeddingPreflightTest(unittest.TestCase):
    def test_reports_missing_packages_without_fabricating_readiness(self):
        with tempfile.TemporaryDirectory() as directory:
            requirements = Path(directory) / "requirements.txt"
            requirements.write_text("package-that-does-not-exist==1.0\n", encoding="utf-8")
            result = check(requirements)
            self.assertEqual("ENVIRONMENT_LIMITED", result["status"])
            self.assertFalse(result["requirements"]["package-that-does-not-exist"]["installed"])
            self.assertEqual("BAAI/bge-base-en-v1.5", result["modelId"])


if __name__ == "__main__":
    unittest.main()
