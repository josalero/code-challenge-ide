#!/usr/bin/env python3
import json
import subprocess
import time

test_src = """package com.challenge;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class HiddenTest {
  @Test void works() { assertEquals("cba", Solution.reverse("abc")); }
}"""
job = {
    "challenge_slug": "bench",
    "solution_code": (
        "package com.challenge;\n"
        "public class Solution { "
        "public static String reverse(String s) { "
        "return new StringBuilder(s).reverse().toString(); } }"
    ),
    "hidden_tests": [{"name": "hidden", "source": test_src}],
    "limits": {"wall_seconds": 120},
}

for i in range(1, 4):
    t0 = time.perf_counter()
    proc = subprocess.run(
        ["python3", "/opt/runner/run.py"],
        input=json.dumps(job),
        text=True,
        capture_output=True,
    )
    dt = time.perf_counter() - t0
    out = json.loads(proc.stdout)
    print(
        f"run{i} {dt:.2f}s status={out.get('status')} "
        f"tests={len(out.get('tests', []))} coverage={out.get('coverage')}"
    )
    if out.get("status") != "COMPLETED":
        logs = out.get("logs") or {}
        print("stderr:", (logs.get("stderr_truncated") or proc.stderr)[:500])
