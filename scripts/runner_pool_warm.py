#!/usr/bin/env python3
"""Smoke-warm runner pool path for every language (docker pool create + exec run.py)."""

from __future__ import annotations

import json
import os
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CHALLENGES = ROOT / "challenges"
MAVEN_CACHE = os.environ.get("RUNNER_MAVEN_CACHE_VOLUME", "ctl-runner-m2-cache")

LIMITS = {
    "memory_mb": 1024,
    "cpus": 2,
    "wall_seconds": 180,
    "cpu_seconds": 60,
    "pids": 512,
    "stdout_bytes": 2097152,
    "per_test_seconds": 30,
}

# Known-good solutions (same as scripts/smoke-runners.sh) — warms runner infra, not starter stubs.
SMOKE_SOLUTIONS: dict[str, str] = {
    "reverse-string": """package com.challenge;

public class Solution {
  public static String reverse(String input) {
    if (input == null) {
      return null;
    }
    return new StringBuilder(input).reverse().toString();
  }
}
""",
    "fizzbuzz-python": """def fizz_buzz(n: int) -> list[str]:
    out = []
    for i in range(1, n + 1):
        s = ""
        if i % 3 == 0:
            s += "Fizz"
        if i % 5 == 0:
            s += "Buzz"
        out.append(s if s else str(i))
    return out
""",
    "gcd-go": """package solution

func Gcd(a, b int) int {
\tfor b != 0 {
\t\ta, b = b, a%b
\t}
\tif a < 0 {
\t\treturn -a
\t}
\treturn a
}
""",
    "gcd-node": """function gcd(a, b) {
  a = Math.abs(a);
  b = Math.abs(b);
  while (b) {
    [a, b] = [b, a % b];
  }
  return a;
}
module.exports = { gcd };
""",
    "gcd-csharp": """namespace Challenge;

public static class Solution
{
    public static int Gcd(int a, int b)
    {
        while (b != 0)
        {
            (a, b) = (b, a % b);
        }
        return Math.Abs(a);
    }
}
""",
    "gcd-typescript": """export function gcd(a: number, b: number): number {
  a = Math.abs(a);
  b = Math.abs(b);
  while (b) {
    [a, b] = [b, a % b];
  }
  return a;
}
""",
    "gcd-rust": """pub fn gcd(a: i32, b: i32) -> i32 {
    let (mut a, mut b) = (a.abs(), b.abs());
    while b != 0 {
        let t = b;
        b = a % b;
        a = t;
    }
    a
}
""",
    "gcd-cpp": """int gcd(int a, int b) {
    if (a < 0) a = -a;
    if (b < 0) b = -b;
    while (b != 0) {
        int t = b;
        b = a % b;
        a = t;
    }
    return a;
}
""",
    "greeting-react": """type GreetingProps = {
  name: string;
};

export function Greeting({ name }: GreetingProps) {
  return <h1>Hello, {name}!</h1>;
}
""",
    "counter-vue": """<script setup lang="ts">
import { ref } from "vue";

const props = withDefaults(defineProps<{ initial?: number }>(), { initial: 0 });
const count = ref(props.initial);

function increment() {
  count.value += 1;
}
</script>

<template>
  <button type="button" @click="increment">{{ count }}</button>
</template>
""",
    "reverse-pipe-angular": """import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: "reverse", standalone: true })
export class ReversePipe implements PipeTransform {
  transform(value: string): string {
    return [...value].reverse().join("");
  }
}
""",
}

# label, image_env, default_image, smoke_slug, workspace_layout
# Slugs align with scripts/smoke-runners.sh (not Admin Ops RunnerSmokeChallenges).
RUNNER_TARGETS: tuple[tuple[str, str, str, str, str], ...] = (
    ("Java 26", "RUNNER_JAVA_26_IMAGE", "code-challenge-ide-runner-java-26:local", "reverse-string", "maven"),
    ("Java 25", "RUNNER_JAVA_25_IMAGE", "code-challenge-ide-runner-java-25:local", "reverse-string", "maven"),
    ("Java 21", "RUNNER_JAVA_21_IMAGE", "code-challenge-ide-runner-java-21:local", "reverse-string", "maven"),
    ("Java 17", "RUNNER_JAVA_17_IMAGE", "code-challenge-ide-runner-java-17:local", "reverse-string", "maven"),
    ("Python", "RUNNER_PYTHON_312_IMAGE", "code-challenge-ide-runner-python-312:local", "fizzbuzz-python", "pytest"),
    ("Go", "RUNNER_GO_123_IMAGE", "code-challenge-ide-runner-go-123:local", "gcd-go", "go-test"),
    ("Node.js", "RUNNER_NODE_22_IMAGE", "code-challenge-ide-runner-node-22:local", "gcd-node", "node-test"),
    ("C#", "RUNNER_DOTNET_8_IMAGE", "code-challenge-ide-runner-dotnet-8:local", "gcd-csharp", "dotnet"),
    ("TypeScript", "RUNNER_TYPESCRIPT_57_IMAGE", "code-challenge-ide-runner-typescript-57:local", "gcd-typescript", "typescript-test"),
    ("Rust", "RUNNER_RUST_184_IMAGE", "code-challenge-ide-runner-rust-184:local", "gcd-rust", "cargo-test"),
    ("C++", "RUNNER_CPP_20_IMAGE", "code-challenge-ide-runner-cpp-20:local", "gcd-cpp", "cmake-test"),
    ("React", "RUNNER_REACT_19_IMAGE", "code-challenge-ide-runner-react-19:local", "greeting-react", "vitest-react"),
    ("Vue", "RUNNER_VUE_35_IMAGE", "code-challenge-ide-runner-vue-35:local", "counter-vue", "vitest-vue"),
    ("Angular", "RUNNER_ANGULAR_19_IMAGE", "code-challenge-ide-runner-angular-19:local", "reverse-pipe-angular", "vitest-angular"),
)


def pool_container_name(image: str) -> str:
    normalized = image.strip().lower().replace("/", "-").replace(":", "-")
    safe = "".join(c if c.isalnum() or c in "._-" else "-" for c in normalized)
    body = safe[:48] if safe else "runner"
    return f"ctl-warm-pool-{body}"


def solution_for(slug: str, challenge_dir: Path) -> str:
    if slug in SMOKE_SOLUTIONS:
        return SMOKE_SOLUTIONS[slug]
    starter_dir = challenge_dir / "starter"
    files = sorted(starter_dir.glob("*")) if starter_dir.is_dir() else []
    if not files:
        raise FileNotFoundError(f"No smoke solution or starter for {slug}")
    return files[0].read_text(encoding="utf-8")


def load_hidden_tests(challenge_dir: Path) -> list[dict[str, str]]:
    hidden_dir = challenge_dir / "hidden" / "tests"
    if not hidden_dir.is_dir():
        return []
    tests: list[dict[str, str]] = []
    for path in sorted(hidden_dir.iterdir()):
        if path.is_file():
            tests.append({"name": path.name, "source": path.read_text(encoding="utf-8")})
    return tests


def infrastructure_failure(status: str, tests: list[dict]) -> bool:
    if status != "FAILED":
        return False
    return len(tests) == 1 and tests[0].get("name") == "runner"


def build_job(slug: str, layout: str, solution: str, hidden: list[dict[str, str]]) -> str:
    return json.dumps(
        {
            "submission_id": "pool-warm",
            "challenge_slug": slug,
            "workspace_layout": layout,
            "solution_code": solution,
            "custom_tests_code": None,
            "hidden_tests": hidden,
            "limits": LIMITS,
        }
    )


def docker_run(args: list[str], *, input_text: str | None = None, timeout: int = 200) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        args,
        input=input_text,
        capture_output=True,
        text=True,
        timeout=timeout,
        check=False,
    )


def warm_one(label: str, image: str, slug: str, layout: str) -> tuple[bool, str, float]:
    started = time.monotonic()
    challenge_dir = CHALLENGES / slug
    if not challenge_dir.is_dir():
        return False, f"missing challenge {slug}", time.monotonic() - started

    try:
        solution = solution_for(slug, challenge_dir)
        hidden: list[dict[str, str]] = []  # match smoke-runners.sh (public tests only)
    except OSError as exc:
        return False, str(exc), time.monotonic() - started

    name = pool_container_name(image)
    subprocess.run(["docker", "rm", "-f", name], capture_output=True, check=False)

    create = [
        "docker",
        "run",
        "-d",
        "--name",
        name,
        "--label",
        "ctl.runner-pool-warm=true",
        "--network",
        "none",
        "--memory",
        "1024m",
        "--cpus",
        "2",
        "--pids-limit",
        "512",
        "--tmpfs",
        "/tmp:rw,exec,size=768m,mode=1777",
        "-e",
        "CTL_RUNNER_POOLED=1",
    ]
    if layout == "maven" and MAVEN_CACHE:
        create.extend(["-v", f"{MAVEN_CACHE}:/tmp/home/.m2:rw"])
    create.extend(["--entrypoint", "sleep", image, "infinity"])

    created = docker_run(create, timeout=90)
    if created.returncode != 0:
        return False, (created.stderr or created.stdout or "create failed").strip(), time.monotonic() - started
    container_id = (created.stdout or "").strip()
    if not container_id:
        return False, "empty container id", time.monotonic() - started

    try:
        sync = docker_run(
            [
                "docker",
                "exec",
                "-u",
                "0",
                container_id,
                "sh",
                "-c",
                "mkdir -p /challenge && rm -rf /challenge/*",
            ],
            timeout=60,
        )
        if sync.returncode != 0:
            return False, "challenge mkdir failed", time.monotonic() - started

        cp = docker_run(["docker", "cp", f"{challenge_dir}/.", f"{container_id}:/challenge/"], timeout=120)
        if cp.returncode != 0:
            return False, (cp.stderr or "docker cp failed").strip(), time.monotonic() - started

        chmod = docker_run(
            ["docker", "exec", "-u", "0", container_id, "chmod", "-R", "a+rX", "/challenge"],
            timeout=60,
        )
        if chmod.returncode != 0:
            return False, "chmod failed", time.monotonic() - started

        job = build_job(slug, layout, solution, hidden)
        exec_run = docker_run(
            ["docker", "exec", "-i", "-e", "PYTHONUNBUFFERED=1", container_id, "python3", "-u", "/opt/runner/run.py"],
            input_text=job,
            timeout=LIMITS["wall_seconds"] + 30,
        )
        line = (exec_run.stdout or "").splitlines()[0] if exec_run.stdout else ""
        if not line:
            detail = (exec_run.stderr or "no runner output").strip()[-300:]
            return False, detail or "no runner output", time.monotonic() - started
        payload = json.loads(line)
        status = payload.get("status", "")
        tests = payload.get("tests") or []
        if infrastructure_failure(status, tests):
            msg = next(
                (t.get("message", "") for t in tests if t.get("name") == "runner"),
                "runner error",
            )
            return False, msg or "runner infrastructure failure", time.monotonic() - started
        if status != "COMPLETED":
            return False, f"status={status}", time.monotonic() - started
        fails = sum(1 for t in tests if t.get("status") == "FAIL")
        if fails:
            return False, f"{fails} failing test(s)", time.monotonic() - started
        return True, "ok", time.monotonic() - started
    except (json.JSONDecodeError, subprocess.TimeoutExpired, OSError) as exc:
        return False, str(exc), time.monotonic() - started
    finally:
        subprocess.run(["docker", "rm", "-f", name], capture_output=True, check=False)


def main() -> int:
    if not CHALLENGES.is_dir():
        print(f"Missing challenges dir: {CHALLENGES}", file=sys.stderr)
        return 1

    failures: list[str] = []
    passed = 0
    print(f"Runner pool warm ({len(RUNNER_TARGETS)} targets)…")
    for label, env_key, default_image, slug, layout in RUNNER_TARGETS:
        image = os.environ.get(env_key, default_image)
        try:
            subprocess.run(["docker", "image", "inspect", image], capture_output=True, check=True, timeout=10)
        except (subprocess.CalledProcessError, subprocess.TimeoutExpired):
            print(f"SKIP {label}: image {image} not found")
            continue

        ok, detail, elapsed = warm_one(label, image, slug, layout)
        if ok:
            print(f"OK   {label} ({elapsed:.1f}s)")
            passed += 1
        else:
            print(f"FAIL {label}: {detail}")
            failures.append(f"{label}: {detail}")

    print(f"Done: {passed} passed, {len(failures)} failed")
    if failures:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
