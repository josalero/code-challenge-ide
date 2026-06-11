#!/usr/bin/env python3
"""PostgreSQL SQL challenge runner — JSON job on stdin, JSON result on stdout."""

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
STAMP = WORKSPACE / ".ctl-challenge-slug"
MAX_LOG_BYTES = 4096
PG_BIN = Path("/usr/lib/postgresql/15/bin")
PGDATA = Path(os.environ.get("CTL_PGDATA", "/tmp/pgdata"))


def read_job() -> dict:
    raw = sys.stdin.read()
    if not raw.strip():
        raise ValueError("empty stdin job")
    return json.loads(raw)


def write_file(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def _run_as_postgres(args: list[str], **kwargs) -> subprocess.CompletedProcess:
    return subprocess.run(
        ["runuser", "-u", "postgres", "--", *args],
        **kwargs,
    )


def ensure_postgres() -> None:
    if not PG_BIN.is_dir():
        raise RuntimeError("PostgreSQL 15 binaries not found in runner image")
    PGDATA.mkdir(parents=True, exist_ok=True)
    subprocess.run(["chown", "-R", "postgres:postgres", str(PGDATA)], check=False)
    if not (PGDATA / "PG_VERSION").is_file():
        initdb = _run_as_postgres(
            [str(PG_BIN / "initdb"), "-D", str(PGDATA), "--auth-local=trust", "--auth-host=trust"],
            capture_output=True,
            text=True,
        )
        if initdb.returncode != 0:
            detail = (initdb.stderr or initdb.stdout or "initdb failed").strip()
            raise RuntimeError(detail)
    status = _run_as_postgres([str(PG_BIN / "pg_ctl"), "-D", str(PGDATA), "status"], capture_output=True, text=True)
    if status.returncode != 0:
        start = _run_as_postgres(
            [
                str(PG_BIN / "pg_ctl"),
                "-D",
                str(PGDATA),
                "-l",
                "/tmp/pg.log",
                "-o",
                "-c listen_addresses=127.0.0.1 -c unix_socket_directories=/tmp",
                "start",
            ],
            capture_output=True,
            text=True,
        )
        if start.returncode != 0:
            detail = (start.stderr or start.stdout or "pg_ctl start failed").strip()
            raise RuntimeError(detail)
    for _ in range(50):
        ready = _run_as_postgres([str(PG_BIN / "pg_isready"), "-h", "127.0.0.1", "-p", "5432"], capture_output=True)
        if ready.returncode == 0:
            return
        time.sleep(0.1)
    raise RuntimeError("PostgreSQL did not become ready")


def _write_solution(job: dict) -> None:
    write_file(WORKSPACE / "solution.sql", job["solution_code"])


def _copy_setup() -> None:
    setup_src = CHALLENGE_MOUNT / "setup"
    if setup_src.is_dir():
        dest = WORKSPACE / "setup"
        if dest.exists():
            shutil.rmtree(dest)
        shutil.copytree(setup_src, dest)


def _write_tests(job: dict) -> None:
    tests_dir = WORKSPACE / "tests"
    if tests_dir.is_dir():
        shutil.rmtree(tests_dir)
    tests_dir.mkdir(parents=True, exist_ok=True)
    shutil.copy(OPT / "ctl_sql.py", WORKSPACE / "ctl_sql.py")
    shutil.copy(OPT / "conftest.py", WORKSPACE / "conftest.py")

    public_dir = CHALLENGE_MOUNT / "public" / "tests"
    if public_dir.is_dir():
        for src in sorted(public_dir.glob("*.py")):
            shutil.copy(src, tests_dir / src.name)

    for hidden in job.get("hidden_tests") or []:
        source = hidden.get("source") or ""
        if source.strip():
            name = hidden.get("name") or "hidden_test"
            if not name.endswith(".py"):
                name = re.sub(r"[^a-zA-Z0-9_]", "_", name) + ".py"
            write_file(tests_dir / name, source)


def _write_all_sources(job: dict) -> None:
    _write_solution(job)
    _copy_setup()
    _write_tests(job)


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
        and (WORKSPACE / "solution.sql").is_file()
    ):
        _write_solution(job)
        _write_tests(job)
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


def run_pytest(wall_seconds: int) -> tuple[int, str, str]:
    cmd = ["python3", "-m", "pytest", "tests", "-q", "--tb=short", "--junitxml=/tmp/workspace/junit.xml"]
    proc = subprocess.run(
        cmd,
        cwd=WORKSPACE,
        capture_output=True,
        text=True,
        timeout=wall_seconds,
        env={
            **os.environ,
            "PYTHONPATH": str(WORKSPACE),
            "CTL_PG_HOST": "127.0.0.1",
            "CTL_PG_PORT": "5432",
            "CTL_PG_DB": "postgres",
            "CTL_PG_USER": "postgres",
            "CTL_PG_PASSWORD": "",
        },
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
                tests.append({"name": name, "status": "FAIL", "message": message, "duration_ms": duration_ms})
            elif skipped is not None:
                tests.append({"name": name, "status": "SKIP", "message": None, "duration_ms": duration_ms})
            else:
                tests.append({"name": name, "status": "PASS", "message": None, "duration_ms": duration_ms})
    return tests


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
        layout = job.get("workspace_layout")
        if layout not in (None, "postgres-sql"):
            emit(failed("Unsupported workspace layout: " + str(layout)))
            return 0
        limits = job.get("limits") or {}
        wall_seconds = int(limits.get("wall_seconds", 120))
        ensure_postgres()
        setup_workspace(job)
        code, stdout_log, stderr_log = run_pytest(wall_seconds)
        tests = parse_junit()
        if not tests and code != 0:
            emit(
                {
                    **failed("pytest failed: " + truncate(stderr_log or stdout_log)),
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
                "coverage": {"line_percent": 0.0, "branch_percent": 0.0},
                "compile": {"warnings": 0, "messages": []},
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
