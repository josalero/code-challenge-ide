#!/usr/bin/env python3
"""Java 26 challenge runner — reads one JSON job line on stdin, writes one JSON result line on stdout.

The default submission flow only runs tests + JaCoCo coverage and surfaces compiler warnings.
Style/security analyzers (Checkstyle, PMD, SpotBugs, …) are invoked on demand by separate jobs.
"""

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
OPT = Path("/opt/runner")
STAMP = WORKSPACE / ".ctl-challenge-slug"
MAX_LOG_BYTES = 4096
MAX_COMPILE_MESSAGES = 20


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
    warm_marker = target / ".warm"
    if warm_marker.is_file() or any(target.iterdir() if target.is_dir() else []):
        return target
    warm = Path("/opt/m2/repository")
    if warm.is_dir():
        shutil.copytree(warm, target, symlinks=True)
        warm_marker.touch()
    return target


def _write_test_sources(job: dict) -> None:
    write_file(WORKSPACE / "src/main/java/com/challenge/Solution.java", job["solution_code"])

    test_root = WORKSPACE / "src/test/java"
    if test_root.is_dir():
        shutil.rmtree(test_root)
    test_root.mkdir(parents=True)

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


def _bootstrap_workspace(pom_text: str) -> None:
    WORKSPACE.mkdir(parents=True, exist_ok=True)
    (WORKSPACE / "pom.xml").write_text(pom_text, encoding="utf-8")


def setup_workspace(job: dict) -> None:
    os.environ["HOME"] = "/tmp/home"
    ensure_m2_repository()

    slug = (job.get("challenge_slug") or "").strip()
    pooled = os.environ.get("CTL_RUNNER_POOLED") == "1"

    pom_src = OPT / "pom-template.xml"
    pom_text = pom_src.read_text(encoding="utf-8")
    if "__JAVA_MAJOR__" in pom_text:
        major = os.environ.get("JAVA_MAJOR", "26")
        pom_text = pom_text.replace("__JAVA_MAJOR__", major)

    if (
        pooled
        and slug
        and WORKSPACE.is_dir()
        and STAMP.is_file()
        and STAMP.read_text(encoding="utf-8") == slug
        and (WORKSPACE / "pom.xml").is_file()
    ):
        _write_test_sources(job)
        return

    if WORKSPACE.exists():
        shutil.rmtree(WORKSPACE)
    _bootstrap_workspace(pom_text)
    _write_test_sources(job)
    if pooled and slug:
        STAMP.write_text(slug, encoding="utf-8")


def truncate(text: str, limit: int = MAX_LOG_BYTES) -> str:
    if len(text) <= limit:
        return text
    return text[: limit - 3] + "..."


def run_maven(wall_seconds: int) -> tuple[int, str, str]:
    m2_repo = ensure_m2_repository()
    base = [
        "mvn",
        "-q",
        "-o",
        "-T",
        "1C",
        f"-Dmaven.repo.local={m2_repo}",
        "-f",
        str(WORKSPACE / "pom.xml"),
    ]
    env = os.environ.copy()
    env["HOME"] = "/tmp/home"
    env.setdefault(
        "MAVEN_OPTS",
        "-XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Xmx512m",
    )
    proc = subprocess.run(
        [*base, "test", "jacoco:report"],
        cwd=WORKSPACE,
        capture_output=True,
        text=True,
        timeout=max(wall_seconds - 5, 30),
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
        return {"line_percent": 0.0, "branch_percent": 0.0}
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

    return {
        "line_percent": percent(line_missed, line_covered),
        "branch_percent": percent(branch_missed, branch_covered),
    }


COMPILE_WARNING_PATTERN = re.compile(
    r"^\[WARNING\]\s+(?P<file>/[^:\s\[]+\.java):\[(?P<line>\d+),\d+\]\s+(?P<message>.+)$"
)


def parse_compile_warnings(stdout: str, stderr: str) -> dict:
    """Extract javac warnings from the Maven output (no extra tool invocation)."""
    messages: list[dict] = []
    for line in (stdout + "\n" + stderr).splitlines():
        match = COMPILE_WARNING_PATTERN.match(line.strip())
        if not match:
            continue
        if len(messages) < MAX_COMPILE_MESSAGES:
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


def process_job(job: dict) -> dict:
    limits = job.get("limits") or {}
    wall_seconds = int(limits.get("wall_seconds", 120))
    setup_workspace(job)
    code, stdout_log, stderr_log = run_maven(wall_seconds)
    tests = parse_surefire()
    if not tests and code != 0:
        return {
            **failed("Maven failed: " + truncate(stderr_log or stdout_log)),
            "logs": {
                "stdout_truncated": truncate(stdout_log),
                "stderr_truncated": truncate(stderr_log),
            },
        }
    return {
        "status": "COMPLETED",
        "tests": tests,
        "coverage": parse_jacoco(),
        "compile": parse_compile_warnings(stdout_log, stderr_log),
        "logs": {
            "stdout_truncated": truncate(stdout_log),
            "stderr_truncated": truncate(stderr_log),
        },
    }


def main() -> int:
    stdout_log = ""
    stderr_log = ""
    try:
        job = read_job()
        emit(process_job(job))
        return 0
    except subprocess.TimeoutExpired:
        emit({**failed("Runner timed out"), "logs": {"stdout_truncated": "", "stderr_truncated": ""}})
        return 0
    except Exception as exc:  # noqa: BLE001
        emit({**failed(str(exc)), "logs": {"stdout_truncated": stdout_log, "stderr_truncated": stderr_log}})
        return 0


if __name__ == "__main__":
    raise SystemExit(main())
