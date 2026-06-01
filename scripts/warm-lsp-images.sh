#!/usr/bin/env bash
# Run an LSP initialize handshake against each language-server image (cold-start verification).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

if [[ "${CTL_SKIP_LSP_WARM:-}" == "1" || "${CTL_SKIP_LSP_WARM:-}" == "true" ]]; then
  echo "Skipping LSP warm (CTL_SKIP_LSP_WARM=${CTL_SKIP_LSP_WARM})."
  exit 0
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "docker not found — skipping LSP warm." >&2
  exit 0
fi

export PYTHONUNBUFFERED=1

if [[ "${CTL_FORCE_LSP_WARM:-}" == "1" || "${CTL_FORCE_LSP_WARM:-}" == "true" ]]; then
  set -- --force "$@"
fi

python3 "${ROOT}/scripts/lsp_warm.py" "$@"
