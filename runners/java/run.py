#!/usr/bin/env python3
"""Java 26 challenge runner — reads one JSON job line on stdin, writes one JSON result line on stdout."""

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


def java_test_path(source: str) -> Path:
    pkg = re.search(r"package\s+([\w.]+)\s*;", source)
    cls = re.search(r"class\s+(\w+)", source)
    if not pkg or not cls:
        raise ValueError("invalid test source: missing package or class")
    rel = pkg.group(1).replace(".", "/")
    return Path("src/test/java") / rel / f"{cls.group(1)}.java"


def write_file(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def ensure_m2_repository() -> Path:
    target = Path("/tmp/home/.m2/repository")
    target.parent.mkdir(parents=True, exist_ok=True)
    warm = Path("/opt/m2/repository")
    if not target.exists() or not any(target.iterdir()):
        if warm.is_dir():
            shutil.copytree(warm, target, symlinks=True)
    return target


def setup_workspace(job: dict) -> None:
    os.environ["HOME"] = "/tmp/home"
    ensure_m2_repository()

    if WORKSPACE.exists():
        shutil.rmtree(WORKSPACE)
    WORKSPACE.mkdir(parents=True)
    pom_src = OPT / "pom-template.xml"
    pom_text = pom_src.read_text(encoding="utf-8")
    if "__JAVA_MAJOR__" in pom_text:
        major = os.environ.get("JAVA_MAJOR", "26")
        pom_text = pom_text.replace("__JAVA_MAJOR__", major)
    (WORKSPACE / "pom.xml").write_text(pom_text, encoding="utf-8")
    shutil.copy(OPT / "checkstyle.xml", WORKSPACE / "checkstyle.xml")

    write_file(WORKSPACE / "src/main/java/com/challenge/Solution.java", job["solution_code"])

    public_dir = CHALLENGE_MOUNT / "public" / "tests"
    if public_dir.is_dir():
        for src in sorted(public_dir.glob("*.java")):
            source = src.read_text(encoding="utf-8")
            write_file(WORKSPACE / java_test_path(source), source)

    for hidden in job.get("hidden_tests") or []:
        source = hidden.get("source") or ""
        if source.strip():
            write_file(WORKSPACE / java_test_path(source), source)

    custom = job.get("custom_tests_code")
    if custom and str(custom).strip():
        write_file(WORKSPACE / java_test_path(custom), custom)


def truncate(text: str, limit: int = MAX_LOG_BYTES) -> str:
    if len(text) <= limit:
        return text
    return text[: limit - 3] + "..."


def run_maven(wall_seconds: int) -> tuple[int, str, str]:
    m2_repo = ensure_m2_repository()
    cmd = [
        "mvn",
        "-q",
        "-o",
        f"-Dmaven.repo.local={m2_repo}",
        "-f",
        str(WORKSPACE / "pom.xml"),
        "test",
        "jacoco:report",
        "checkstyle:checkstyle",
    ]
    env = os.environ.copy()
    env["HOME"] = "/tmp/home"
    proc = subprocess.run(
        cmd,
        cwd=WORKSPACE,
        capture_output=True,
        text=True,
        timeout=wall_seconds,
        env=env,
    )
    return proc.returncode, proc.stdout, proc.stderr


def parse_surefire() -> list[dict]:
    reports = WORKSPACE / "target" / "surefire-reports"
    tests: list[dict] = []
    if not reports.is_dir():
        return tests
    for xml_file in sorted(reports.glob("TEST-*.xml")):
        root = ET.parse(xml_file).getroot()
        suite_name = root.attrib.get("name", xml_file.stem)
        for case in root.findall("testcase"):
            name = f"{suite_name}.{case.attrib.get('name', 'unknown')}"
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


def parse_jacoco() -> dict:
    xml_path = WORKSPACE / "target" / "site" / "jacoco" / "jacoco.xml"
    if not xml_path.is_file():
        return {"line_percent": 0.0, "branch_percent": 0.0, "raw_path": str(xml_path)}
    root = ET.parse(xml_path).getroot()
    line_missed = line_covered = branch_missed = branch_covered = 0
    for counter in root.iter("counter"):
        ctype = counter.attrib.get("type")
        missed = int(counter.attrib.get("missed", "0"))
        covered = int(counter.attrib.get("covered", "0"))
        if ctype == "LINE":
            line_missed += missed
            line_covered += covered
        elif ctype == "BRANCH":
            branch_missed += missed
            branch_covered += covered

    def percent(missed: int, covered: int) -> float:
        total = missed + covered
        return 0.0 if total == 0 else round(100.0 * covered / total, 1)

    line_cov = percent(line_missed, line_covered)
    branch_cov = percent(branch_missed, branch_covered)
    return {
        "line_percent": line_cov,
        "branch_percent": branch_cov,
        "raw_path": str(xml_path),
    }


def parse_checkstyle() -> dict:
    xml_path = WORKSPACE / "target" / "checkstyle-result.xml"
    errors = warnings = 0
    findings: list[dict] = []
    if xml_path.is_file():
        root = ET.parse(xml_path).getroot()
        for file_node in root.findall("file"):
            for err in file_node.findall("error"):
                severity = err.attrib.get("severity", "error")
                if severity == "warning":
                    warnings += 1
                else:
                    errors += 1
                if len(findings) < 20:
                    findings.append(
                        {
                            "file": file_node.attrib.get("name"),
                            "line": err.attrib.get("line"),
                            "message": err.attrib.get("message"),
                        }
                    )
    return {"errors": errors, "warnings": warnings, "findings": findings}


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
    started = time.time()
    stdout_log = ""
    stderr_log = ""
    try:
        job = read_job()
        limits = job.get("limits") or {}
        wall_seconds = int(limits.get("wall_seconds", 120))
        setup_workspace(job)
        code, stdout_log, stderr_log = run_maven(wall_seconds)
        tests = parse_surefire()
        if not tests and code != 0:
            emit(
                {
                    **failed("Maven failed: " + truncate(stderr_log or stdout_log)),
                    "logs": {
                        "stdout_truncated": truncate(stdout_log),
                        "stderr_truncated": truncate(stderr_log),
                    },
                }
            )
            return 0
        status = "COMPLETED"
        emit(
            {
                "status": status,
                "tests": tests,
                "coverage": parse_jacoco(),
                "checkstyle": parse_checkstyle(),
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
