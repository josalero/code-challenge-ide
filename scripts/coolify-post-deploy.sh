#!/usr/bin/env bash
# Run on the Coolify host after compose deploy (pull GHCR runners, build host-only images).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

if [[ -z "${DOCKER_GID:-}" ]] && [[ -S /var/run/docker.sock ]]; then
  if stat -c '%g' /var/run/docker.sock >/dev/null 2>&1; then
    export DOCKER_GID="$(stat -c '%g' /var/run/docker.sock)"
  elif stat -f '%g' /var/run/docker.sock >/dev/null 2>&1; then
    export DOCKER_GID="$(stat -f '%g' /var/run/docker.sock)"
  fi
  echo "Using DOCKER_GID=${DOCKER_GID:-0} for runner/LSP containers"
fi

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

if [[ -z "${CTL_IMAGE_OWNER:-}" ]]; then
  echo "Set CTL_IMAGE_OWNER in Coolify env (GitHub username, lowercase) or in .env" >&2
  exit 1
fi

echo "=== Pulling GHCR runner + lsp-java images ==="
"${ROOT}/scripts/pull-runner-images.sh"

echo "=== Building runner/LSP images not yet on GHCR (SQL + non-Java LSP) ==="
docker compose -f docker-compose.yml build \
  runner-postgres-17 \
  runner-lsp-python \
  runner-lsp-go \
  runner-lsp-typescript \
  runner-lsp-dotnet \
  runner-lsp-rust \
  runner-lsp-cpp

if [[ -x "${ROOT}/scripts/smoke-runners.sh" ]]; then
  echo "=== Smoke-check runners (optional) ==="
  "${ROOT}/scripts/smoke-runners.sh" || echo "smoke-runners.sh reported issues — check Ops after first login"
fi

echo "Post-deploy complete. Open Admin → Ops and run Warm everything if RUNNER_POOL_WARM_ON_STARTUP is false."
