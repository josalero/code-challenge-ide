#!/usr/bin/env bash
# End-to-end warm verification: ephemeral runner smoke, pooled runner warm, LSP warm.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

FAIL=0

run_step() {
  local name="$1"
  shift
  echo ""
  echo "========== ${name} =========="
  if "$@"; then
    echo ">>> ${name}: OK"
  else
    echo ">>> ${name}: FAILED" >&2
    FAIL=$((FAIL + 1))
  fi
}

run_step "Runner smoke (11 languages, ephemeral docker run)" "${ROOT}/scripts/smoke-runners.sh"
run_step "Runner pool warm (all runtimes + Java 17/21/25/26)" python3 "${ROOT}/scripts/runner_pool_warm.py"
run_step "LSP warm (all servers + Vue)" \
  env CTL_FORCE_LSP_WARM=1 python3 "${ROOT}/scripts/lsp_warm.py" --force --include-vue --parallel 2

echo ""
if [[ "${FAIL}" -gt 0 ]]; then
  echo "${FAIL} step(s) failed."
  exit 1
fi
echo "All warm tests passed."
