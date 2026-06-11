#!/usr/bin/env bash
# Smoke-test every language runner via docker run (requires: make runners).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

CHALLENGES="${ROOT}/challenges"
MAVEN_CACHE="${RUNNER_MAVEN_CACHE_VOLUME:-ctl-runner-m2-cache}"
FAIL=0
PASS=0

resolve_runner_image() {
  local explicit="${1:-}"
  local default_image="$2"
  if [[ -n "${explicit}" ]] && docker image inspect "${explicit}" >/dev/null 2>&1; then
    printf '%s' "${explicit}"
    return 0
  fi
  if docker image inspect "${default_image}" >/dev/null 2>&1; then
    if [[ -n "${explicit}" && "${explicit}" != "${default_image}" ]]; then
      echo "  Note: ${explicit} unavailable locally; using ${default_image}" >&2
    fi
    printf '%s' "${default_image}"
    return 0
  fi
  if [[ -n "${explicit}" ]]; then
    printf '%s' "${explicit}"
  else
    printf '%s' "${default_image}"
  fi
  return 1
}

run_smoke() {
  local name="$1"
  local image="$2"
  local slug="$3"
  local layout="$4"
  local solution_file="$5"

  if ! docker image inspect "${image}" >/dev/null 2>&1; then
    echo "SKIP ${name}: image ${image} not found (run: make runners)"
    return 0
  fi

  local mount="${CHALLENGES}/${slug}"
  if [[ ! -d "${mount}" ]]; then
    echo "FAIL ${name}: missing challenge ${slug}"
    FAIL=$((FAIL + 1))
    return 1
  fi

  local job
  job="$(python3 - "${layout}" "${solution_file}" <<'PY'
import json, sys
layout = sys.argv[1]
with open(sys.argv[2], encoding="utf-8") as f:
    solution = f.read()
print(json.dumps({
    "submission_id": "smoke",
    "workspace_layout": layout,
    "solution_code": solution,
    "hidden_tests": [],
    "limits": {
        "memory_mb": 1024,
        "cpus": 2,
        "wall_seconds": 180,
        "cpu_seconds": 60,
        "pids": 512,
        "stdout_bytes": 2097152,
        "per_test_seconds": 30,
    },
}))
PY
)"

  local -a docker_args=(
    docker run --rm -i
    --network none
    --cap-drop ALL
    --cap-add FOWNER
    --security-opt no-new-privileges:true
    --ipc none
    --memory 1024m
    --cpus 2
    --pids-limit 512
  )
  if [[ "${layout}" != "postgres-sql" ]]; then
    docker_args+=(--read-only)
  else
    docker_args+=(--cap-add SETUID --cap-add SETGID --cap-add CHOWN)
  fi
  docker_args+=(
    --tmpfs "/tmp:rw,exec,size=768m,mode=1777"
    -v "${mount}:/challenge:ro"
  )
  if [[ "${layout}" == "maven" && -n "${MAVEN_CACHE}" ]]; then
    docker_args+=(-v "${MAVEN_CACHE}:/tmp/home/.m2:rw")
  fi
  docker_args+=("${image}")

  local out
  if ! out="$(printf '%s' "${job}" | "${docker_args[@]}" 2>/dev/null | head -1)"; then
    echo "FAIL ${name}: docker run error"
    FAIL=$((FAIL + 1))
    return 1
  fi

  local status
  status="$(python3 -c 'import json,sys; print(json.loads(sys.argv[1]).get("status",""))' "${out}" 2>/dev/null || echo "")"
  if [[ "${status}" != "COMPLETED" ]]; then
    echo "FAIL ${name}: status=${status:-unknown}"
    echo "${out}" | head -c 400
    echo
    FAIL=$((FAIL + 1))
    return 1
  fi

  local fails
  fails="$(python3 -c '
import json, sys
r = json.loads(sys.argv[1])
tests = r.get("tests") or []
print(sum(1 for t in tests if t.get("status") == "FAIL"))
' "${out}" 2>/dev/null || echo "?")"
  if [[ "${fails}" != "0" ]]; then
    echo "FAIL ${name}: ${fails} failing test(s)"
    FAIL=$((FAIL + 1))
    return 1
  fi
  echo "OK   ${name}"
  PASS=$((PASS + 1))
}

TMP="$(mktemp -d)"
trap 'rm -rf "${TMP}"' EXIT

cat >"${TMP}/reverse.java" <<'EOF'
package com.challenge;

public class Solution {
  public static String reverse(String input) {
    if (input == null) {
      return null;
    }
    return new StringBuilder(input).reverse().toString();
  }
}
EOF

cat >"${TMP}/fizzbuzz.py" <<'EOF'
def fizz_buzz(n: int) -> list[str]:
    out = []
    for i in range(1, n + 1):
        s = ""
        if i % 3 == 0:
            s += "Fizz"
        if i % 5 == 0:
            s += "Buzz"
        out.append(s if s else str(i))
    return out
EOF

cat >"${TMP}/gcd.go" <<'EOF'
package solution

func Gcd(a, b int) int {
	for b != 0 {
		a, b = b, a%b
	}
	if a < 0 {
		return -a
	}
	return a
}
EOF

cat >"${TMP}/gcd.js" <<'EOF'
function gcd(a, b) {
  a = Math.abs(a);
  b = Math.abs(b);
  while (b) {
    [a, b] = [b, a % b];
  }
  return a;
}
module.exports = { gcd };
EOF

cat >"${TMP}/gcd.cs" <<'EOF'
namespace Challenge;

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
EOF

cat >"${TMP}/gcd.ts" <<'EOF'
export function gcd(a: number, b: number): number {
  a = Math.abs(a);
  b = Math.abs(b);
  while (b) {
    [a, b] = [b, a % b];
  }
  return a;
}
EOF

cat >"${TMP}/lib.rs" <<'EOF'
pub fn gcd(a: i32, b: i32) -> i32 {
    let (mut a, mut b) = (a.abs(), b.abs());
    while b != 0 {
        let t = b;
        b = a % b;
        a = t;
    }
    a
}
EOF

cat >"${TMP}/gcd.cpp" <<'EOF'
int gcd(int a, int b) {
    if (a < 0) a = -a;
    if (b < 0) b = -b;
    while (b != 0) {
        int t = b;
        b = a % b;
        a = t;
    }
    return a;
}
EOF

cat >"${TMP}/greeting.tsx" <<'EOF'
type GreetingProps = {
  name: string;
};

export function Greeting({ name }: GreetingProps) {
  return <h1>Hello, {name}!</h1>;
}
EOF

cat >"${TMP}/counter.vue" <<'EOF'
<script setup lang="ts">
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
EOF

cat >"${TMP}/reverse-pipe.ts" <<'EOF'
import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: "reverse", standalone: true })
export class ReversePipe implements PipeTransform {
  transform(value: string): string {
    return [...value].reverse().join("");
  }
}
EOF

cat >"${TMP}/sql-count.sql" <<'EOF'
SELECT COUNT(*) AS engineering_count
FROM employees
WHERE department_id = 2;
EOF

echo "Smoke-testing runners (12 languages)..."
run_smoke "Java" "$(resolve_runner_image "${RUNNER_JAVA_26_IMAGE:-}" code-challenge-ide-runner-java-26:local)" reverse-string maven "${TMP}/reverse.java"
run_smoke "Python" "$(resolve_runner_image "${RUNNER_PYTHON_312_IMAGE:-}" code-challenge-ide-runner-python-312:local)" fizzbuzz-python pytest "${TMP}/fizzbuzz.py"
run_smoke "Go" "$(resolve_runner_image "${RUNNER_GO_123_IMAGE:-}" code-challenge-ide-runner-go-123:local)" gcd-go go-test "${TMP}/gcd.go"
run_smoke "Node.js" "$(resolve_runner_image "${RUNNER_NODE_22_IMAGE:-}" code-challenge-ide-runner-node-22:local)" gcd-node node-test "${TMP}/gcd.js"
run_smoke "C#" "$(resolve_runner_image "${RUNNER_DOTNET_8_IMAGE:-}" code-challenge-ide-runner-dotnet-8:local)" gcd-csharp dotnet "${TMP}/gcd.cs"
run_smoke "TypeScript" "$(resolve_runner_image "${RUNNER_TYPESCRIPT_57_IMAGE:-}" code-challenge-ide-runner-typescript-57:local)" gcd-typescript typescript-test "${TMP}/gcd.ts"
run_smoke "Rust" "$(resolve_runner_image "${RUNNER_RUST_184_IMAGE:-}" code-challenge-ide-runner-rust-184:local)" gcd-rust cargo-test "${TMP}/lib.rs"
run_smoke "C++" "$(resolve_runner_image "${RUNNER_CPP_20_IMAGE:-}" code-challenge-ide-runner-cpp-20:local)" gcd-cpp cmake-test "${TMP}/gcd.cpp"
run_smoke "React" "$(resolve_runner_image "${RUNNER_REACT_19_IMAGE:-}" code-challenge-ide-runner-react-19:local)" greeting-react vitest-react "${TMP}/greeting.tsx"
run_smoke "Vue" "$(resolve_runner_image "${RUNNER_VUE_35_IMAGE:-}" code-challenge-ide-runner-vue-35:local)" counter-vue vitest-vue "${TMP}/counter.vue"
run_smoke "Angular" "$(resolve_runner_image "${RUNNER_ANGULAR_19_IMAGE:-}" code-challenge-ide-runner-angular-19:local)" reverse-pipe-angular vitest-angular "${TMP}/reverse-pipe.ts"
run_smoke "SQL" "$(resolve_runner_image "${RUNNER_POSTGRES_17_IMAGE:-}" code-challenge-ide-runner-postgres-17:local)" sql-count-engineering postgres-sql "${TMP}/sql-count.sql"

echo "Done: ${PASS} passed, ${FAIL} failed"
if [[ "${FAIL}" -gt 0 ]]; then
  exit 1
fi
