#!/usr/bin/env python3
"""Vue 3 challenge runner (Vitest + Vue Test Utils) — JSON on stdin/stdout."""

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
MAX_LOG_BYTES = 4096
LAYOUT = "vitest-vue"


def read_job() -> dict:
    raw = sys.stdin.read()
    if not raw.strip():
        raise ValueError("empty stdin job")
    return json.loads(raw)


def write_file(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def copy_runner_deps() -> None:
    for name in ("package.json", "vitest.config.ts", "tsconfig.json"):
        shutil.copy(OPT / name, WORKSPACE / name)
    src_modules = OPT / "node_modules"
    if src_modules.is_dir():
        dest = WORKSPACE / "node_modules"
        if dest.exists() or dest.is_symlink():
            dest.unlink()
        os.symlink(src_modules, dest, target_is_directory=True)


def setup_workspace(job: dict) -> None:
    if WORKSPACE.exists():
        shutil.rmtree(WORKSPACE)
    WORKSPACE.mkdir(parents=True)
    copy_runner_deps()
    write_file(WORKSPACE / "solution.vue", job["solution_code"])

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
                name = re.sub(r"[^a-zA-Z0-9_.-]", "_", name) + ".test.ts"
            write_file(TESTS_DIR / name, source)

    custom = job.get("custom_tests_code")
    if custom and str(custom).strip():
        write_file(TESTS_DIR / "custom.test.ts", custom)


def truncate(text: str, limit: int = MAX_LOG_BYTES) -> str:
    if len(text) <= limit:
        return text
    return text[: limit - 3] + "..."


def run_vitest(wall_seconds: int) -> tuple[int, str, str]:
    junit = WORKSPACE / "junit.xml"
    proc = subprocess.run(
        ["npx", "vitest", "run", "--reporter=junit", f"--outputFile={junit}", "--coverage"],
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
    for case in root.findall(".//testcase"):
        name = case.attrib.get("name", "unknown")
        classname = case.attrib.get("classname", "")
        full = f"{classname}.{name}" if classname else name
        duration_ms = int(float(case.attrib.get("time", "0")) * 1000)
        failure = case.find("failure")
        error = case.find("error")
        if failure is not None or error is not None:
            node = failure if failure is not None else error
            message = (node.attrib.get("message") or node.text or "failed").strip()
            tests.append({"name": full, "status": "FAIL", "message": message, "duration_ms": duration_ms})
        elif case.find("skipped") is not None:
            tests.append({"name": full, "status": "SKIP", "message": None, "duration_ms": duration_ms})
        else:
            tests.append({"name": full, "status": "PASS", "message": None, "duration_ms": duration_ms})
    return tests


def parse_coverage() -> dict:
    summary = WORKSPACE / "coverage" / "coverage-summary.json"
    if not summary.is_file():
        return {"line_percent": 0.0, "branch_percent": 0.0}
    data = json.loads(summary.read_text(encoding="utf-8"))
    lines = data.get("total", {}).get("lines", {})
    return {"line_percent": round(float(lines.get("pct", 0.0)), 1), "branch_percent": 0.0}


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


def main() -> int:
    stdout_log = ""
    stderr_log = ""
    try:
        job = read_job()
        if job.get("workspace_layout") not in (None, LAYOUT):
            emit(failed("Unsupported workspace layout: " + str(job.get("workspace_layout"))))
            return 0
        wall_seconds = int((job.get("limits") or {}).get("wall_seconds", 180))
        setup_workspace(job)
        code, stdout_log, stderr_log = run_vitest(wall_seconds)
        tests = parse_junit()
        if not tests and code != 0:
            emit(
                {
                    **failed("vitest failed: " + truncate(stderr_log or stdout_log)),
                    "logs": {"stdout_truncated": truncate(stdout_log), "stderr_truncated": truncate(stderr_log)},
                }
            )
            return 0
        emit(
            {
                "status": "COMPLETED",
                "tests": tests,
                "coverage": parse_coverage(),
                "checkstyle": {"errors": 0, "warnings": 0},
                "logs": {"stdout_truncated": truncate(stdout_log), "stderr_truncated": truncate(stderr_log)},
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
