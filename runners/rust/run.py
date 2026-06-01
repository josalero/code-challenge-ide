#!/usr/bin/env python3
"""Rust challenge runner — JSON job on stdin, JSON result on stdout."""

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
OPT = Path("/opt/runner")
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
    write_file(WORKSPACE / "src" / "lib.rs", job["solution_code"])
    custom = job.get("custom_tests_code")
    if custom and str(custom).strip():
        write_file(TESTS_DIR / "custom_tests.rs", custom)


def _write_all_sources(job: dict) -> None:
    shutil.copy(OPT / "Cargo.toml", WORKSPACE / "Cargo.toml")
    (WORKSPACE / "src").mkdir(parents=True, exist_ok=True)
    _write_solution(job)

    if TESTS_DIR.is_dir():
        shutil.rmtree(TESTS_DIR)
    TESTS_DIR.mkdir(parents=True, exist_ok=True)

    public_dir = CHALLENGE_MOUNT / "public" / "tests"
    if public_dir.is_dir():
        for src in sorted(public_dir.glob("*.rs")):
            shutil.copy(src, TESTS_DIR / src.name)

    for hidden in job.get("hidden_tests") or []:
        source = hidden.get("source") or ""
        if source.strip():
            name = hidden.get("name") or "hidden_test"
            if not name.endswith(".rs"):
                name = re.sub(r"[^a-zA-Z0-9_]", "_", name) + ".rs"
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
        and (WORKSPACE / "Cargo.toml").is_file()
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


def run_cargo_test(wall_seconds: int) -> tuple[int, str, str]:
    proc = subprocess.run(
        [
            "cargo",
            "llvm-cov",
            "test",
            "--quiet",
            "--summary-only",
            "--",
            "--show-output",
        ],
        cwd=WORKSPACE,
        capture_output=True,
        text=True,
        timeout=wall_seconds,
    )
    return proc.returncode, proc.stdout, proc.stderr


def run_coverage_from_output(stdout: str, stderr: str) -> dict:
    line_percent = 0.0
    for line in (stdout + stderr).splitlines():
        pct = re.search(r"(\d+\.\d+)%", line)
        if pct and "TOTAL" in line:
            line_percent = float(pct.group(1))
            break
    return {"line_percent": round(line_percent, 1), "branch_percent": 0.0}


def parse_cargo_output(stdout: str, stderr: str) -> list[dict]:
    tests: list[dict] = []
    combined = stdout + "\n" + stderr
    for line in combined.splitlines():
        line = line.strip()
        ok = re.match(r"test\s+(\S+)\s+\.\.\.\s+ok", line)
        fail = re.match(r"test\s+(\S+)\s+\.\.\.\s+FAILED", line)
        if ok:
            tests.append({"name": ok.group(1), "status": "PASS", "message": None, "duration_ms": 0})
        elif fail:
            tests.append({"name": fail.group(1), "status": "FAIL", "message": "failed", "duration_ms": 0})
    return tests



def parse_compile_warnings(stderr: str) -> dict:
    messages: list[dict] = []
    pending_message: str | None = None
    for line in stderr.splitlines():
        warn = re.match(r"^warning:\s+(.+)$", line.strip())
        if warn:
            pending_message = warn.group(1).strip()
            continue
        loc = re.match(r"^\s+-->\s+([^:]+):(\d+):\d+", line.strip())
        if loc and pending_message and len(messages) < 20:
            messages.append(
                {
                    "file": loc.group(1),
                    "line": int(loc.group(2)),
                    "message": pending_message,
                }
            )
            pending_message = None
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


def configure_cargo_home() -> None:
    cargo_home = Path("/tmp/cargo-home")
    cargo_home.mkdir(parents=True, exist_ok=True)
    os.environ["CARGO_HOME"] = str(cargo_home)
    target_dir = Path("/tmp/cargo-target")
    target_dir.mkdir(parents=True, exist_ok=True)
    os.environ["CARGO_TARGET_DIR"] = str(target_dir)


def main() -> int:
    stdout_log = ""
    stderr_log = ""
    try:
        configure_cargo_home()
        job = read_job()
        layout = job.get("workspace_layout")
        if layout not in (None, "cargo-test"):
            emit(failed("Unsupported workspace layout: " + str(layout)))
            return 0
        limits = job.get("limits") or {}
        wall_seconds = int(limits.get("wall_seconds", 120))
        setup_workspace(job)
        code, stdout_log, stderr_log = run_cargo_test(wall_seconds)
        tests = parse_cargo_output(stdout_log, stderr_log)
        if not tests and code != 0:
            emit(
                {
                    **failed("cargo test failed: " + truncate(stderr_log or stdout_log)),
                    "compile": parse_compile_warnings(stderr_log),
                    "logs": {
                        "stdout_truncated": truncate(stdout_log),
                        "stderr_truncated": truncate(stderr_log),
                    },
                }
            )
            return 0
        coverage = (
            run_coverage_from_output(stdout_log, stderr_log)
            if code == 0
            else {"line_percent": 0.0, "branch_percent": 0.0}
        )
        emit(
            {
                "status": "COMPLETED",
                "tests": tests,
                "coverage": coverage,
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
