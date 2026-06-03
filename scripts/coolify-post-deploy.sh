#!/usr/bin/env bash
# Optional: rebuild runner + LSP images on the host (local Docker context).
# Coolify default deploy does not build profile `runners` — run this after deploy or when runners/ change.
# Use after changes under runners/ or if Ops reports missing :local images.
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

# Build the local tags expected by Flyway language_runtimes and the Ops page.
# Coolify env files may point some RUNNER_* values at GHCR for pull-based deploys;
# this script is the local-build path, so keep its output aligned with the DB.
export RUNNER_JAVA_25_IMAGE=code-challenge-ide-runner-java-25:local
export RUNNER_JAVA_26_IMAGE=code-challenge-ide-runner-java-26:local
export RUNNER_PYTHON_312_IMAGE=code-challenge-ide-runner-python-312:local
export RUNNER_GO_123_IMAGE=code-challenge-ide-runner-go-123:local
export RUNNER_NODE_22_IMAGE=code-challenge-ide-runner-node-22:local
export RUNNER_DOTNET_8_IMAGE=code-challenge-ide-runner-dotnet-8:local
export RUNNER_TYPESCRIPT_57_IMAGE=code-challenge-ide-runner-typescript-57:local
export RUNNER_RUST_184_IMAGE=code-challenge-ide-runner-rust-184:local
export RUNNER_CPP_20_IMAGE=code-challenge-ide-runner-cpp-20:local
export RUNNER_REACT_19_IMAGE=code-challenge-ide-runner-react-19:local
export RUNNER_VUE_35_IMAGE=code-challenge-ide-runner-vue-35:local
export RUNNER_ANGULAR_19_IMAGE=code-challenge-ide-runner-angular-19:local
export RUNNER_POSTGRES_17_IMAGE=code-challenge-ide-runner-postgres-17:local
export LSP_JAVA_IMAGE=code-challenge-ide-lsp-java:local
export LSP_PYTHON_IMAGE=code-challenge-ide-lsp-python:local
export LSP_GO_IMAGE=code-challenge-ide-lsp-go:local
export LSP_TYPESCRIPT_IMAGE=code-challenge-ide-lsp-typescript:local
export LSP_DOTNET_IMAGE=code-challenge-ide-lsp-dotnet:local
export LSP_RUST_IMAGE=code-challenge-ide-lsp-rust:local
export LSP_CPP_IMAGE=code-challenge-ide-lsp-cpp:local

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

echo "=== Building runner + LSP images (:local tags) ==="
docker compose -f docker-compose.coolify.yml --profile runners build "${RUNNER_SERVICES[@]}"

if [[ -x "${ROOT}/scripts/smoke-runners.sh" ]]; then
  echo "=== Smoke-check runners (optional) ==="
  "${ROOT}/scripts/smoke-runners.sh" || echo "smoke-runners.sh reported issues — check Ops after first login"
fi

echo "Post-deploy complete. Open Admin → Ops and run Warm everything if RUNNER_POOL_WARM_ON_STARTUP is false."
