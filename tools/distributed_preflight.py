#!/usr/bin/env python3
"""Check whether the distributed research Compose environment is executable."""

import argparse
import json
import subprocess
from pathlib import Path


def run(command, cwd):
    return subprocess.run(command, cwd=cwd, capture_output=True, text=True)


def check(root):
    docker = run(["docker", "info"], root)
    compose = run(["docker", "compose", "-f", "infrastructure/docker/compose.research-distributed.yaml", "config", "--quiet"], root)
    if docker.returncode != 0:
        return {"status": "ENVIRONMENT_LIMITED", "docker": docker.stderr.strip() or "docker info failed", "composeConfig": compose.returncode == 0}
    if compose.returncode != 0:
        return {"status": "CONFIG_INVALID", "docker": "ready", "composeError": compose.stderr.strip()}
    return {"status": "READY", "docker": "ready", "composeConfig": True}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    result = check(args.root.resolve())
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, sort_keys=True))
    return 0 if result["status"] == "READY" else 1


if __name__ == "__main__": raise SystemExit(main())
