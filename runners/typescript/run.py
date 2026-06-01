#!/usr/bin/env python3
"""TypeScript challenge runner (Node 22 + tsx) — JSON job on stdin, JSON result on stdout."""

from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

WORKSPACE = Path("/tmp/workspace")
CHALLENGE_MOUNT = Path("/challenge")
TESTS_DIR = WORKSPACE / "tests"
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
    write_file(WORKSPACE / "package.json", '{"type":"module"}\n')
    eslintrc = Path("/opt/runner/.eslintrc.json")
    if eslintrc.is_file():
        shutil.copy(eslintrc, WORKSPACE / ".eslintrc.json")
    write_file(WORKSPACE / "solution.ts", job["solution_code"])

    TESTS_DIR.mkdir(parents=True, exist_ok=True)
    public_dir = CHALLENGE_MOUNT / "public" / "tests"
    if public_dir.is_dir():
        for src in sorted(public_dir.glob("*.test.ts")):
            shutil.copy(src, TESTS_DIR / src.name)

    for hidden in job.get("hidden_tests") or []:
        source = hidden.get("source") or ""
        if source.strip():
            name = hidden.get("name") or "hidden_test"
            if not name.endswith(".test.ts"):
                name = re.sub(r"[^a-zA-Z0-9_.]", "_", name) + ".test.ts"
            write_file(TESTS_DIR / name, source)

    custom = job.get("custom_tests_code")
    if custom and str(custom).strip():
        write_file(TESTS_DIR / "custom.test.ts", custom)


def truncate(text: str, limit: int = MAX_LOG_BYTES) -> str:
    if len(text) <= limit:
        return text
    return text[: limit - 3] + "..."


def run_tests(wall_seconds: int) -> tuple[int, str, str]:
    junit = WORKSPACE / "junit.xml"
    cmd = [
        "c8",
        "--reporter=json-summary",
        "--temp-directory",
        str(WORKSPACE / ".c8_tmp"),
        "npx",
        "tsx",
        "--test",
        "--test-reporter",
        "junit",
        "--test-reporter-destination",
        str(junit),
        "tests/*.test.ts",
    ]
    proc = subprocess.run(
        cmd,
        cwd=WORKSPACE,
        capture_output=True,
        text=True,
        timeout=wall_seconds,
    )
    return proc.returncode, proc.stdout, proc.stderr


def run_eslint() -> tuple[int, str]:
    proc = subprocess.run(
        ["eslint", "solution.ts", "tests", "--format", "compact"],
        cwd=WORKSPACE,
        capture_output=True,
        text=True,
        timeout=30,
    )
    return proc.returncode, proc.stdout + proc.stderr


def parse_junit() -> list[dict]:
    xml_path = WORKSPACE / "junit.xml"
    tests: list[dict] = []
    if not xml_path.is_file():
        return tests
    root = ET.parse(xml_path).getroot()
    for suite in root.findall("testsuite"):
        for case in suite.findall("testcase"):
            name = case.attrib.get("name", "unknown")
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


def parse_c8_coverage() -> dict:
    summary = WORKSPACE / "coverage" / "coverage-summary.json"
    if not summary.is_file():
        return {"line_percent": 0.0, "branch_percent": 0.0}
    data = json.loads(summary.read_text(encoding="utf-8"))
    total = data.get("total", {})
    lines = total.get("lines", {})
    pct = float(lines.get("pct", 0.0))
    return {"line_percent": round(pct, 1), "branch_percent": 0.0}


def parse_eslint(output: str) -> dict:
    errors = len(re.findall(r"Error -", output))
    warnings = len(re.findall(r"Warning -", output))
    return {"errors": errors, "warnings": warnings, "findings": []}


def emit(result: dict) -> None:
    sys.stdout.write(json.dumps(result, separators=(",", ":")))
    sys.stdout.flush()


def failed(message: str) -> dict:
    return {
        "status": "FAILED",
        "tests": [{"name": "runner", "status": "FAIL", "message": message, "duration_ms": 0}],
        "coverage": {"line_percent": 0.0, "branch_percent": 0.0},
        "checkstyle": {"errors": 0, "warnings": 0},
    }


def configure_node_cache() -> None:
    cache = Path("/tmp/npm-cache")
    cache.mkdir(parents=True, exist_ok=True)
    os.environ["HOME"] = "/tmp"
    os.environ["npm_config_cache"] = str(cache)


def main() -> int:
    stdout_log = ""
    stderr_log = ""
    try:
        configure_node_cache()
        job = read_job()
        layout = job.get("workspace_layout")
        if layout not in (None, "typescript-test"):
            emit(failed("Unsupported workspace layout: " + str(layout)))
            return 0
        limits = job.get("limits") or {}
        wall_seconds = int(limits.get("wall_seconds", 120))
        setup_workspace(job)
        code, stdout_log, stderr_log = run_tests(wall_seconds)
        tests = parse_junit()
        eslint_code, eslint_out = run_eslint()
        if not tests and code != 0:
            emit(
                {
                    **failed("typescript test failed: " + truncate(stderr_log or stdout_log)),
                    "checkstyle": parse_eslint(eslint_out),
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
                "coverage": parse_c8_coverage(),
                "checkstyle": parse_eslint(eslint_out if eslint_code else ""),
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
