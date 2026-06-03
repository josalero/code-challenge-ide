#!/usr/bin/env bash
# Coolify post-deploy: build runner + LSP images on the host (local Docker context).
# App images (be/fe) are built by docker-compose.coolify.yml during deploy.
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

RUNNER_SERVICES=(
  runner-java-25
  runner-java-26
  runner-python-312
  runner-go-123
  runner-node-22
  runner-dotnet-8
  runner-typescript-57
  runner-rust-184
  runner-cpp-20
  runner-react-19
  runner-vue-35
  runner-angular-19
  runner-postgres-17
  runner-lsp-java
  runner-lsp-python
  runner-lsp-go
  runner-lsp-typescript
  runner-lsp-dotnet
  runner-lsp-rust
  runner-lsp-cpp
)

echo "=== Building runner + LSP images (docker-compose.coolify.yml, :local tags) ==="
docker compose -f docker-compose.coolify.yml build "${RUNNER_SERVICES[@]}"

if [[ -x "${ROOT}/scripts/smoke-runners.sh" ]]; then
  echo "=== Smoke-check runners (optional) ==="
  "${ROOT}/scripts/smoke-runners.sh" || echo "smoke-runners.sh reported issues — check Ops after first login"
fi

echo "Post-deploy complete. Open Admin → Ops and run Warm everything if RUNNER_POOL_WARM_ON_STARTUP is false."
