#!/usr/bin/env python3
"""C# / .NET 8 challenge runner — JSON job on stdin, JSON result on stdout."""

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
TESTS_DIR = WORKSPACE / "Tests"
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
    write_file(WORKSPACE / "Solution.cs", job["solution_code"])
    custom = job.get("custom_tests_code")
    if custom and str(custom).strip():
        write_file(TESTS_DIR / "CustomTests.cs", custom)


def _write_all_sources(job: dict) -> None:
    shutil.copy(OPT / "Challenge.csproj", WORKSPACE / "Challenge.csproj")
    opt_obj = OPT / "obj"
    if opt_obj.is_dir():
        shutil.copytree(opt_obj, WORKSPACE / "obj")
    _write_solution(job)

    if TESTS_DIR.is_dir():
        shutil.rmtree(TESTS_DIR)
    TESTS_DIR.mkdir(parents=True, exist_ok=True)

    public_dir = CHALLENGE_MOUNT / "public" / "tests"
    if public_dir.is_dir():
        for src in sorted(public_dir.glob("*.cs")):
            shutil.copy(src, TESTS_DIR / src.name)

    for hidden in job.get("hidden_tests") or []:
        source = hidden.get("source") or ""
        if source.strip():
            name = hidden.get("name") or "HiddenTests"
            if not name.endswith(".cs"):
                name = re.sub(r"[^a-zA-Z0-9_]", "_", name) + ".cs"
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
        and (WORKSPACE / "Challenge.csproj").is_file()
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


def run_dotnet_test(wall_seconds: int) -> tuple[int, str, str]:
    trx = WORKSPACE / "results.trx"
    cmd = [
        "dotnet",
        "test",
        "Challenge.csproj",
        "--no-restore",
        "--nologo",
        "-v",
        "quiet",
        "--logger",
        f"trx;LogFileName={trx}",
        "--collect",
        "XPlat Code Coverage",
        "--results-directory",
        str(WORKSPACE / "TestResults"),
    ]
    proc = subprocess.run(
        cmd,
        cwd=WORKSPACE,
        capture_output=True,
        text=True,
        timeout=wall_seconds,
    )
    return proc.returncode, proc.stdout, proc.stderr


def restore_workspace() -> tuple[int, str]:
    """Fallback when image obj cache is missing (offline restore into shared package store)."""
    proc = subprocess.run(
        [
            "dotnet",
            "restore",
            "Challenge.csproj",
            "--packages",
            "/opt/nuget-packages",
            "--disable-parallel",
        ],
        cwd=WORKSPACE,
        capture_output=True,
        text=True,
        timeout=120,
    )
    return proc.returncode, proc.stdout + proc.stderr


def parse_compile_warnings(output: str) -> dict:
    messages: list[dict] = []
    pattern = re.compile(
        r"^(?P<file>[^(\s]+)\((?P<line>\d+),\d+\):\s+warning\s+(?P<code>CS\d+):\s+(?P<message>.+)$"
    )
    for line in output.splitlines():
        match = pattern.match(line.strip())
        if match and len(messages) < 20:
            messages.append(
                {
                    "file": match.group("file"),
                    "line": int(match.group("line")),
                    "message": f"{match.group('code')}: {match.group('message').strip()}",
                }
            )
    return {"warnings": len(messages), "messages": messages}


def parse_trx() -> list[dict]:
    trx_path = WORKSPACE / "results.trx"
    tests: list[dict] = []
    if not trx_path.is_file():
        return tests
    root = ET.parse(trx_path).getroot()
    for unit in root.iter():
        if not unit.tag.endswith("UnitTestResult"):
            continue
        name = unit.attrib.get("testName", "unknown")
        outcome = unit.attrib.get("outcome", "Failed")
        duration = unit.attrib.get("duration", "PT0S")
        duration_ms = 0
        match = re.match(r"PT(?:(\d+)H)?(?:(\d+)M)?(?:([\d.]+)S)?", duration)
        if match:
            hours = int(match.group(1) or 0)
            minutes = int(match.group(2) or 0)
            seconds = float(match.group(3) or 0)
            duration_ms = int((hours * 3600 + minutes * 60 + seconds) * 1000)
        if outcome == "Passed":
            status = "PASS"
            message = None
        elif outcome == "Skipped":
            status = "SKIP"
            message = None
        else:
            status = "FAIL"
            message = outcome
            for child in unit.iter():
                if child.tag.endswith("Message") and child.text:
                    message = child.text.strip()
                    break
        tests.append(
            {"name": name, "status": status, "message": message, "duration_ms": duration_ms}
        )
    return tests


def parse_coverage() -> dict:
    results_dir = WORKSPACE / "TestResults"
    if not results_dir.is_dir():
        return {"line_percent": 0.0, "branch_percent": 0.0}
    for cobertura in results_dir.rglob("coverage.cobertura.xml"):
        root = ET.parse(cobertura).getroot()
        line_rate = float(root.attrib.get("line-rate", "0")) * 100.0
        branch_rate = float(root.attrib.get("branch-rate", "0")) * 100.0
        return {
            "line_percent": round(line_rate, 1),
            "branch_percent": round(branch_rate, 1),
        }
    return {"line_percent": 0.0, "branch_percent": 0.0}


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


def configure_dotnet_home() -> None:
    home = Path("/tmp/dotnet-home")
    home.mkdir(parents=True, exist_ok=True)
    os.environ["HOME"] = str(home)
    os.environ["DOTNET_CLI_HOME"] = str(home)
    # Pre-restored offline in the image (see Dockerfile).
    os.environ["NUGET_PACKAGES"] = "/opt/nuget-packages"


def main() -> int:
    stdout_log = ""
    stderr_log = ""
    try:
        configure_dotnet_home()
        job = read_job()
        layout = job.get("workspace_layout")
        if layout not in (None, "dotnet"):
            emit(failed("Unsupported workspace layout: " + str(layout)))
            return 0
        limits = job.get("limits") or {}
        wall_seconds = int(limits.get("wall_seconds", 120))
        setup_workspace(job)
        if not (WORKSPACE / "obj").is_dir():
            restore_code, restore_log = restore_workspace()
            if restore_code != 0:
                emit(
                    {
                        **failed("dotnet restore failed: " + truncate(restore_log)),
                        "logs": {
                            "stdout_truncated": "",
                            "stderr_truncated": truncate(restore_log),
                        },
                    }
                )
                return 0
        code, stdout_log, stderr_log = run_dotnet_test(wall_seconds)
        tests = parse_trx()
        build_log = stdout_log + "\n" + stderr_log
        if not tests and code != 0:
            emit(
                {
                    **failed("dotnet test failed: " + truncate(stderr_log or stdout_log)),
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
                "coverage": parse_coverage(),
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
