import unittest
from pathlib import Path
from unittest.mock import patch
from types import SimpleNamespace

from distributed_preflight import check


class DistributedPreflightTest(unittest.TestCase):
    def test_reports_environment_limited_when_docker_daemon_is_unavailable(self):
        with patch("distributed_preflight.run", side_effect=[SimpleNamespace(returncode=1, stderr="daemon unavailable"), SimpleNamespace(returncode=0, stderr="")]):
            result = check(Path("."))
        self.assertEqual("ENVIRONMENT_LIMITED", result["status"])

    def test_reports_ready_when_docker_and_compose_are_valid(self):
        with patch("distributed_preflight.run", side_effect=[SimpleNamespace(returncode=0, stderr=""), SimpleNamespace(returncode=0, stderr="")]):
            result = check(Path("."))
        self.assertEqual("READY", result["status"])


if __name__ == "__main__": unittest.main()
