#!/usr/bin/env python3
"""Long-lived pooled runner — one JSON job per line on stdin, one JSON result line on stdout."""

from __future__ import annotations

import json
import sys

import run


def main() -> int:
    while True:
        line = sys.stdin.readline()
        if not line:
            break
        line = line.strip()
        if not line:
            continue
        try:
            job = json.loads(line)
            result = run.process_job(job)
        except Exception as exc:  # noqa: BLE001
            result = run.failed(str(exc))
        sys.stdout.write(json.dumps(result, separators=(",", ":")))
        sys.stdout.write("\n")
        sys.stdout.flush()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
