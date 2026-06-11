#!/usr/bin/env python3
"""Node.js challenge runner — JSON job on stdin, JSON result on stdout."""

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
STAMP = WORKSPACE / ".ctl-challenge-slug"
MAX_LOG_BYTES = 4096


def read_job() -> dict:
    raw = sys.stdin.read()
    if not raw.strip():
        raise ValueError("empty stdin job")
    return json.loads(raw)


def write_file(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def _write_solution(job: dict) -> None:
    write_file(WORKSPACE / "solution.js", job["solution_code"])
    custom = job.get("custom_tests_code")
    if custom and str(custom).strip():
        write_file(TESTS_DIR / "custom.test.js", custom)


def _write_test_sources(job: dict) -> None:
    if TESTS_DIR.is_dir():
        shutil.rmtree(TESTS_DIR)
    TESTS_DIR.mkdir(parents=True, exist_ok=True)

    public_dir = CHALLENGE_MOUNT / "public" / "tests"
    if public_dir.is_dir():
        for src in sorted(public_dir.glob("*.test.js")):
            shutil.copy(src, TESTS_DIR / src.name)

    for hidden in job.get("hidden_tests") or []:
        source = hidden.get("source") or ""
        if source.strip():
            name = hidden.get("name") or "hidden_test"
            if not name.endswith(".test.js"):
                name = re.sub(r"[^a-zA-Z0-9_.]", "_", name) + ".test.js"
            write_file(TESTS_DIR / name, source)


def _write_all_sources(job: dict) -> None:
    _write_solution(job)
    _write_test_sources(job)


def _invalidate_test_outputs() -> None:
    for path in (WORKSPACE / "junit.xml", WORKSPACE / "coverage"):
        if path.is_file():
            path.unlink(missing_ok=True)
        elif path.is_dir():
            shutil.rmtree(path, ignore_errors=True)
    c8_tmp = WORKSPACE / ".c8_tmp"
    if c8_tmp.is_dir():
        shutil.rmtree(c8_tmp, ignore_errors=True)


def _is_warm_smoke(job: dict) -> bool:
    return (job.get("submission_id") or "").startswith("warm-")


def setup_workspace(job: dict) -> None:
    slug = (job.get("challenge_slug") or "").strip()
    pooled = os.environ.get("CTL_RUNNER_POOLED") == "1"
    warm_smoke = _is_warm_smoke(job)

    if warm_smoke:
        if WORKSPACE.exists():
            shutil.rmtree(WORKSPACE)
        WORKSPACE.mkdir(parents=True)
        _write_all_sources(job)
        if pooled and slug:
            STAMP.write_text(slug, encoding="utf-8")
        return

    if (
        pooled
        and slug
        and WORKSPACE.is_dir()
        and STAMP.is_file()
        and STAMP.read_text(encoding="utf-8") == slug
        and (WORKSPACE / "solution.js").is_file()
    ):
        _write_solution(job)
        _write_test_sources(job)
        _invalidate_test_outputs()
        return

    if WORKSPACE.exists():
        shutil.rmtree(WORKSPACE)
    WORKSPACE.mkdir(parents=True)
    _write_all_sources(job)
    if pooled and slug:
        STAMP.write_text(slug, encoding="utf-8")


def truncate(text: str, limit: int = MAX_LOG_BYTES) -> str:
    if len(text) <= limit:
        return text
    return text[: limit - 3] + "..."


def safe_float(value: object, default: float = 0.0) -> float:
    try:
        if value is None or value == "":
            return default
        return float(value)
    except (TypeError, ValueError):
        return default


def run_tests(wall_seconds: int, *, collect_coverage: bool = True) -> tuple[int, str, str]:
    junit = WORKSPACE / "junit.xml"
    test_files = sorted(TESTS_DIR.glob("*.test.js"))
    if not test_files:
        return 1, "", "no test files under tests/"
    test_args = [str(path.relative_to(WORKSPACE)) for path in test_files]
    if collect_coverage:
        cmd = [
            "c8",
            "--reporter=json-summary",
            "--temp-directory",
            str(WORKSPACE / ".c8_tmp"),
            "node",
            "--test",
            "--test-reporter",
            "junit",
            "--test-reporter-destination",
            str(junit),
            *test_args,
        ]
    else:
        cmd = [
            "node",
            "--test",
            "--test-reporter",
            "junit",
            "--test-reporter-destination",
            str(junit),
            *test_args,
        ]
    proc = subprocess.run(
        cmd,
        cwd=WORKSPACE,
        capture_output=True,
        text=True,
        timeout=wall_seconds,
    )
    return proc.returncode, proc.stdout, proc.stderr


def parse_c8_coverage() -> dict:
    summary = WORKSPACE / "coverage" / "coverage-summary.json"
    if not summary.is_file():
        return {"line_percent": 0.0, "branch_percent": 0.0}
    data = json.loads(summary.read_text(encoding="utf-8"))
    total = data.get("total", {})
    lines = total.get("lines", {})
    pct = safe_float(lines.get("pct", 0.0))
    return {"line_percent": round(pct, 1), "branch_percent": 0.0}


def _iter_junit_cases(root: ET.Element) -> list[ET.Element]:
    """Node's junit reporter may emit <testcase> under <testsuites> or nested <testsuite>."""
    cases: list[ET.Element] = []
    if root.tag == "testsuites":
        cases.extend(root.findall("testcase"))
        for suite in root.findall("testsuite"):
            cases.extend(suite.findall("testcase"))
    elif root.tag == "testsuite":
        cases.extend(root.findall("testcase"))
    return cases


def parse_junit() -> list[dict]:
    xml_path = WORKSPACE / "junit.xml"
    tests: list[dict] = []
    if not xml_path.is_file():
        return tests
    root = ET.parse(xml_path).getroot()
    for case in _iter_junit_cases(root):
        name = case.attrib.get("name", "unknown")
        raw_time = case.attrib.get("time", "0")
        duration_ms = int(safe_float(raw_time) * 1000)
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


def parse_compile_warnings(stderr: str) -> dict:
    messages: list[dict] = []
    pattern = re.compile(r"^(.+\.js):(\d+):\d+\s+(?:Warning|warning)\s+\w+:\s+(.+)$")
    for line in stderr.splitlines():
        match = pattern.match(line.strip())
        if match and len(messages) < 20:
            messages.append(
                {
                    "file": match.group(1),
                    "line": int(match.group(2)),
                    "message": match.group(3).strip(),
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
        if layout not in (None, "node-test"):
            emit(failed("Unsupported workspace layout: " + str(layout)))
            return 0
        limits = job.get("limits") or {}
        wall_seconds = int(limits.get("wall_seconds", 120))
        warm_smoke = _is_warm_smoke(job)
        setup_workspace(job)
        code, stdout_log, stderr_log = run_tests(
            wall_seconds, collect_coverage=not warm_smoke
        )
        tests = parse_junit()
        if not tests and code != 0:
            emit(
                {
                    **failed("node test failed: " + truncate(stderr_log or stdout_log)),
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
                "coverage": parse_c8_coverage(),
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
