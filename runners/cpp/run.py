#!/usr/bin/env python3
"""C++ challenge runner (CMake + Catch2) — JSON job on stdin, JSON result on stdout."""

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
OPT = Path("/opt/runner")
BUILD_DIR = WORKSPACE / "build"
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
    write_file(WORKSPACE / "solution.cpp", job["solution_code"])
    custom = job.get("custom_tests_code")
    if custom and str(custom).strip():
        write_file(TESTS_DIR / "custom_tests.cpp", custom)


def _write_all_sources(job: dict) -> None:
    shutil.copy(OPT / "CMakeLists.txt", WORKSPACE / "CMakeLists.txt")
    _write_solution(job)

    if TESTS_DIR.is_dir():
        shutil.rmtree(TESTS_DIR)
    TESTS_DIR.mkdir(parents=True, exist_ok=True)

    public_dir = CHALLENGE_MOUNT / "public" / "tests"
    if public_dir.is_dir():
        for src in sorted(public_dir.glob("*.cpp")):
            shutil.copy(src, TESTS_DIR / src.name)

    for hidden in job.get("hidden_tests") or []:
        source = hidden.get("source") or ""
        if source.strip():
            name = hidden.get("name") or "hidden_test"
            if not name.endswith(".cpp"):
                name = re.sub(r"[^a-zA-Z0-9_]", "_", name) + ".cpp"
            write_file(TESTS_DIR / name, source)


def setup_workspace(job: dict) -> None:
    slug = (job.get("challenge_slug") or "").strip()
    pooled = os.environ.get("CTL_RUNNER_POOLED") == "1"

    if (
        pooled
        and slug
        and WORKSPACE.is_dir()
        and STAMP.is_file()
        and STAMP.read_text(encoding="utf-8") == slug
        and (WORKSPACE / "CMakeLists.txt").is_file()
    ):
        _write_solution(job)
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


def run_build_and_test(wall_seconds: int) -> tuple[int, str, str, Path]:
    BUILD_DIR.mkdir(exist_ok=True)
    cmake_cache = BUILD_DIR / "CMakeCache.txt"
    if not cmake_cache.is_file():
        cmake = subprocess.run(
            [
                "cmake",
                "-S",
                str(WORKSPACE),
                "-B",
                str(BUILD_DIR),
                "-DFETCHCONTENT_SOURCE_DIR_CATCH2=/opt/catch2-src",
                "-DFETCHCONTENT_UPDATES_DISCONNECTED=ON",
            ],
            capture_output=True,
            text=True,
            timeout=120,
        )
        if cmake.returncode != 0:
            return cmake.returncode, cmake.stdout, cmake.stderr, BUILD_DIR

    build = subprocess.run(
        ["cmake", "--build", str(BUILD_DIR), "-j", "2"],
        capture_output=True,
        text=True,
        timeout=wall_seconds,
    )
    if build.returncode != 0:
        return build.returncode, build.stdout, build.stderr, BUILD_DIR

    junit = BUILD_DIR / "junit.xml"
    test_bin = BUILD_DIR / "challenge_tests"
    proc = subprocess.run(
        [str(test_bin), "--reporter", "junit", "--out", str(junit)],
        cwd=BUILD_DIR,
        capture_output=True,
        text=True,
        timeout=wall_seconds,
    )
    return proc.returncode, proc.stdout, proc.stderr, junit


def parse_compile_warnings(build_output: str) -> dict:
    messages: list[dict] = []
    pattern = re.compile(
        r"^(?P<file>[^:\s]+\.(?:cpp|hpp|h)):(?P<line>\d+):\d+:\s+warning:\s+(?P<message>.+)$"
    )
    for line in build_output.splitlines():
        match = pattern.match(line.strip())
        if match and len(messages) < 20:
            messages.append(
                {
                    "file": match.group("file"),
                    "line": int(match.group("line")),
                    "message": match.group("message").strip(),
                }
            )
    return {"warnings": len(messages), "messages": messages}


def parse_junit(junit_path: Path) -> list[dict]:
    tests: list[dict] = []
    if not junit_path.is_file():
        return tests
    root = ET.parse(junit_path).getroot()
    for case in root.findall(".//testcase"):
        name = case.attrib.get("name", "unknown")
        classname = case.attrib.get("classname", "")
        full_name = f"{classname}.{name}" if classname else name
        duration_ms = int(float(case.attrib.get("time", "0")) * 1000)
        failure = case.find("failure")
        error = case.find("error")
        skipped = case.find("skipped")
        if failure is not None or error is not None:
            node = failure if failure is not None else error
            message = (node.attrib.get("message") or node.text or "failed").strip()
            tests.append(
                {"name": full_name, "status": "FAIL", "message": message, "duration_ms": duration_ms}
            )
        elif skipped is not None:
            tests.append(
                {"name": full_name, "status": "SKIP", "message": None, "duration_ms": duration_ms}
            )
        else:
            tests.append(
                {"name": full_name, "status": "PASS", "message": None, "duration_ms": duration_ms}
            )
    return tests


def parse_coverage() -> dict:
    gcda_files = list(BUILD_DIR.rglob("*.gcda"))
    if not gcda_files:
        return {"line_percent": 0.0, "branch_percent": 0.0}
    subprocess.run(
        ["lcov", "--capture", "--directory", str(BUILD_DIR), "--output-file", str(WORKSPACE / "coverage.info")],
        cwd=WORKSPACE,
        capture_output=True,
        text=True,
        timeout=60,
    )
    info = WORKSPACE / "coverage.info"
    if not info.is_file():
        return {"line_percent": 0.0, "branch_percent": 0.0}
    proc = subprocess.run(
        ["lcov", "--summary", str(info)],
        capture_output=True,
        text=True,
        timeout=30,
    )
    line_percent = 0.0
    for line in proc.stdout.splitlines():
        if "lines" in line.lower() and "%" in line:
            match = re.search(r"(\d+\.\d+)%", line)
            if match:
                line_percent = float(match.group(1))
    return {"line_percent": round(line_percent, 1), "branch_percent": 0.0}


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


def configure_tool_cache() -> None:
    os.environ["HOME"] = "/tmp"


def main() -> int:
    stdout_log = ""
    stderr_log = ""
    try:
        configure_tool_cache()
        job = read_job()
        layout = job.get("workspace_layout")
        if layout not in (None, "cmake-test"):
            emit(failed("Unsupported workspace layout: " + str(layout)))
            return 0
        limits = job.get("limits") or {}
        wall_seconds = int(limits.get("wall_seconds", 180))
        setup_workspace(job)
        code, stdout_log, stderr_log, junit_path = run_build_and_test(wall_seconds)
        tests = parse_junit(junit_path)
        build_log = stdout_log + "\n" + stderr_log
        coverage = parse_coverage() if code == 0 else {"line_percent": 0.0, "branch_percent": 0.0}
        if not tests and code != 0:
            emit(
                {
                    **failed("C++ build/test failed: " + truncate(stderr_log or stdout_log)),
                    "compile": parse_compile_warnings(build_log),
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
                "coverage": coverage,
                "compile": parse_compile_warnings(build_log),
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
