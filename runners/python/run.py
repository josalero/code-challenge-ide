#!/usr/bin/env python3
"""Python challenge runner — one JSON job on stdin, one JSON result on stdout."""

from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

WORKSPACE = Path("/tmp/workspace")
CHALLENGE_MOUNT = Path("/challenge")
OPT = Path("/opt/runner")
MAX_LOG_BYTES = 4096


def read_job() -> dict:
    raw = sys.stdin.read()
    if not raw.strip():
        raise ValueError("empty stdin job")
    return json.loads(raw)


def write_file(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def setup_workspace(job: dict) -> None:
    if WORKSPACE.exists():
        shutil.rmtree(WORKSPACE)
    WORKSPACE.mkdir(parents=True)
    write_file(WORKSPACE / "solution.py", job["solution_code"])

    public_dir = CHALLENGE_MOUNT / "public" / "tests"
    if public_dir.is_dir():
        tests_dir = WORKSPACE / "tests"
        tests_dir.mkdir(parents=True, exist_ok=True)
        for src in sorted(public_dir.glob("*.py")):
            shutil.copy(src, tests_dir / src.name)

    tests_dir = WORKSPACE / "tests"
    tests_dir.mkdir(parents=True, exist_ok=True)
    for hidden in job.get("hidden_tests") or []:
        source = hidden.get("source") or ""
        if source.strip():
            name = hidden.get("name") or "hidden_test"
            if not name.endswith(".py"):
                name = re.sub(r"[^a-zA-Z0-9_]", "_", name) + ".py"
            write_file(tests_dir / name, source)

    custom = job.get("custom_tests_code")
    if custom and str(custom).strip():
        write_file(tests_dir / "custom_tests.py", custom)


def truncate(text: str, limit: int = MAX_LOG_BYTES) -> str:
    if len(text) <= limit:
        return text
    return text[: limit - 3] + "..."


def run_pytest(wall_seconds: int) -> tuple[int, str, str]:
    cmd = [
        "python",
        "-m",
        "pytest",
        "tests",
        "-q",
        "--tb=short",
        "--cov=solution",
        "--cov-report=xml:/tmp/workspace/coverage.xml",
        "--junitxml=/tmp/workspace/junit.xml",
    ]
    proc = subprocess.run(
        cmd,
        cwd=WORKSPACE,
        capture_output=True,
        text=True,
        timeout=wall_seconds,
    )
    return proc.returncode, proc.stdout, proc.stderr


def parse_junit() -> list[dict]:
    xml_path = WORKSPACE / "junit.xml"
    tests: list[dict] = []
    if not xml_path.is_file():
        return tests
    root = ET.parse(xml_path).getroot()
    for suite in root.findall("testsuite"):
        for case in suite.findall("testcase"):
            name = case.attrib.get("classname", "") + "." + case.attrib.get("name", "unknown")
            duration_ms = int(float(case.attrib.get("time", "0")) * 1000)
            failure = case.find("failure")
            error = case.find("error")
            skipped = case.find("skipped")
            if failure is not None or error is not None:
                node = failure if failure is not None else error
                message = (node.attrib.get("message") or node.text or "failed").strip()
                tests.append(
                    {"name": name, "status": "FAIL", "message": message, "duration_ms": duration_ms}
                )
            elif skipped is not None:
                tests.append({"name": name, "status": "SKIP", "message": None, "duration_ms": duration_ms})
            else:
                tests.append({"name": name, "status": "PASS", "message": None, "duration_ms": duration_ms})
    return tests


def parse_coverage() -> dict:
    xml_path = WORKSPACE / "coverage.xml"
    if not xml_path.is_file():
        return {"line_percent": 0.0, "branch_percent": 0.0}
    root = ET.parse(xml_path).getroot()
    line_rate = float(root.attrib.get("line-rate", "0")) * 100.0
    branch_rate = float(root.attrib.get("branch-rate", "0")) * 100.0
    return {
        "line_percent": round(line_rate, 1),
        "branch_percent": round(branch_rate, 1),
    }


def parse_compile_warnings(stderr: str) -> dict:
    """Surface pytest collection warnings and python SyntaxWarnings from stderr."""
    messages: list[dict] = []
    for line in stderr.splitlines():
        match = re.match(r"^(.+\.py):(\d+):\s*(SyntaxWarning|DeprecationWarning|UserWarning):\s*(.+)$", line)
        if match and len(messages) < 20:
            messages.append(
                {
                    "file": match.group(1),
                    "line": int(match.group(2)),
                    "message": f"{match.group(3)}: {match.group(4)}",
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


def main() -> int:
    stdout_log = ""
    stderr_log = ""
    try:
        job = read_job()
        if job.get("workspace_layout") not in (None, "pytest"):
            emit(failed("Unsupported workspace layout: " + str(job.get("workspace_layout"))))
            return 0
        limits = job.get("limits") or {}
        wall_seconds = int(limits.get("wall_seconds", 120))
        setup_workspace(job)
        code, stdout_log, stderr_log = run_pytest(wall_seconds)
        tests = parse_junit()
        if not tests and code != 0:
            emit(
                {
                    **failed("pytest failed: " + truncate(stderr_log or stdout_log)),
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
