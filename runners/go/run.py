#!/usr/bin/env python3
"""Go challenge runner — JSON job on stdin, JSON result on stdout."""

from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

WORKSPACE = Path("/tmp/workspace")
CHALLENGE_MOUNT = Path("/challenge")
TESTS_DIR = WORKSPACE / "tests"
MAX_LOG_BYTES = 4096
GO_MOD = """module challenge

go 1.23
"""


def read_job() -> dict:
    raw = sys.stdin.read()
    if not raw.strip():
        raise ValueError("empty stdin job")
    return json.loads(raw)


def write_file(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def setup_workspace(job: dict) -> None:
    if os.environ.get("CTL_RUNNER_POOLED") == "1":
        cache = Path("/tmp/gocache")
        cache.mkdir(parents=True, exist_ok=True)
        os.environ["GOCACHE"] = str(cache)

    if WORKSPACE.exists():
        shutil.rmtree(WORKSPACE)
    WORKSPACE.mkdir(parents=True)
    write_file(WORKSPACE / "go.mod", GO_MOD)
    write_file(WORKSPACE / "solution" / "solution.go", job["solution_code"])

    TESTS_DIR.mkdir(parents=True, exist_ok=True)
    public_dir = CHALLENGE_MOUNT / "public" / "tests"
    if public_dir.is_dir():
        for src in sorted(public_dir.glob("*_test.go")):
            shutil.copy(src, TESTS_DIR / src.name)

    for hidden in job.get("hidden_tests") or []:
        source = hidden.get("source") or ""
        if source.strip():
            name = hidden.get("name") or "hidden_test"
            if not name.endswith("_test.go"):
                name = re.sub(r"[^a-zA-Z0-9_]", "_", name) + "_test.go"
            write_file(TESTS_DIR / name, source)

    custom = job.get("custom_tests_code")
    if custom and str(custom).strip():
        write_file(TESTS_DIR / "custom_tests_test.go", custom)


def truncate(text: str, limit: int = MAX_LOG_BYTES) -> str:
    if len(text) <= limit:
        return text
    return text[: limit - 3] + "..."


def run_go_test(wall_seconds: int) -> tuple[int, str, str]:
    cmd = [
        "go",
        "test",
        "./...",
        "-count=1",
        "-coverprofile=coverage.out",
        "-json",
    ]
    proc = subprocess.run(
        cmd,
        cwd=WORKSPACE,
        capture_output=True,
        text=True,
        timeout=wall_seconds,
    )
    return proc.returncode, proc.stdout, proc.stderr


def parse_go_json(stdout: str) -> list[dict]:
    tests: list[dict] = []
    for line in stdout.splitlines():
        line = line.strip()
        if not line:
            continue
        try:
            event = json.loads(line)
        except json.JSONDecodeError:
            continue
        action = event.get("Action")
        test_name = event.get("Test")
        if not test_name or test_name == "main":
            continue
        if action == "pass":
            tests.append(
                {
                    "name": test_name,
                    "status": "PASS",
                    "message": None,
                    "duration_ms": int(float(event.get("Elapsed", 0)) * 1000),
                }
            )
        elif action == "fail":
            tests.append(
                {
                    "name": test_name,
                    "status": "FAIL",
                    "message": (event.get("Output") or "failed").strip(),
                    "duration_ms": int(float(event.get("Elapsed", 0)) * 1000),
                }
            )
        elif action == "skip":
            tests.append(
                {
                    "name": test_name,
                    "status": "SKIP",
                    "message": None,
                    "duration_ms": 0,
                }
            )
    return tests


def parse_coverage() -> dict:
    out_path = WORKSPACE / "coverage.out"
    if not out_path.is_file():
        return {"line_percent": 0.0, "branch_percent": 0.0}
    proc = subprocess.run(
        ["go", "tool", "cover", "-func=coverage.out"],
        cwd=WORKSPACE,
        capture_output=True,
        text=True,
        timeout=30,
    )
    line_percent = 0.0
    for line in proc.stdout.splitlines():
        if line.strip().startswith("total:"):
            match = re.search(r"(\d+\.\d+)%", line)
            if match:
                line_percent = float(match.group(1))
    return {"line_percent": round(line_percent, 1), "branch_percent": 0.0}


GO_COMPILE_WARNING = re.compile(r"^(?P<file>[^:\s]+\.go):(?P<line>\d+):\d+:\s+(?P<message>.+)$")


def parse_compile_warnings(stderr: str) -> dict:
    messages: list[dict] = []
    for line in stderr.splitlines():
        match = GO_COMPILE_WARNING.match(line.strip())
        if not match:
            continue
        if len(messages) < 20:
            messages.append(
                {
                    "file": match.group("file"),
                    "line": int(match.group("line")),
                    "message": match.group("message").strip(),
                }
            )
    return {"warnings": len(messages), "messages": messages}


def emit(result: dict) -> None:
    sys.stdout.write(json.dumps(result, separators=(",", ":")))
    sys.stdout.flush()


def failed(message: str) -> dict:
    return {
        "status": "FAILED",
        "tests": [{"name": "runner", "status": "FAIL", "message": message, "duration_ms": 0}],
        "coverage": {"line_percent": 0.0, "branch_percent": 0.0},
        "compile": {"warnings": 0, "messages": []},
    }


def configure_go_cache() -> None:
    """Use /tmp caches — API runs containers with --read-only root."""
    cache_root = Path("/tmp/go")
    build_cache = cache_root / "build"
    mod_cache = cache_root / "mod"
    build_cache.mkdir(parents=True, exist_ok=True)
    mod_cache.mkdir(parents=True, exist_ok=True)
    os.environ["GOCACHE"] = str(build_cache)
    os.environ["GOMODCACHE"] = str(mod_cache)


def main() -> int:
    stdout_log = ""
    stderr_log = ""
    try:
        configure_go_cache()
        job = read_job()
        layout = job.get("workspace_layout")
        if layout not in (None, "go-test"):
            emit(failed("Unsupported workspace layout: " + str(layout)))
            return 0
        limits = job.get("limits") or {}
        wall_seconds = int(limits.get("wall_seconds", 120))
        setup_workspace(job)
        code, stdout_log, stderr_log = run_go_test(wall_seconds)
        tests = parse_go_json(stdout_log)
        if not tests and code != 0:
            emit(
                {
                    **failed("go test failed: " + truncate(stderr_log or stdout_log)),
                    "compile": parse_compile_warnings(stderr_log),
                    "logs": {
                        "stdout_truncated": truncate(stdout_log),
                        "stderr_truncated": truncate(stderr_log),
                    },
                }
            )
            return 0
        emit(
            {
                "status": "COMPLETED",
                "tests": tests,
                "coverage": parse_coverage(),
                "compile": parse_compile_warnings(stderr_log),
                "logs": {
                    "stdout_truncated": truncate(stdout_log),
                    "stderr_truncated": truncate(stderr_log),
                },
            }
        )
        return 0
    except subprocess.TimeoutExpired:
        emit({**failed("Runner timed out"), "logs": {"stdout_truncated": "", "stderr_truncated": ""}})
        return 0
    except Exception as exc:  # noqa: BLE001
        emit({**failed(str(exc)), "logs": {"stdout_truncated": stdout_log, "stderr_truncated": stderr_log}})
        return 0


if __name__ == "__main__":
    raise SystemExit(main())
