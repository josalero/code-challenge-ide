#!/usr/bin/env bash
# Tag GHCR runner/LSP images as :local names expected by language_runtimes (Flyway) and LSP defaults.
# Run after pull-runner-images.sh on Coolify (or any host that pulls ghcr.io/.../code-challenge-ide-pro-*).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

REGISTRY="${CTL_IMAGE_REGISTRY:-ghcr.io}"
OWNER="${CTL_IMAGE_OWNER:?Set CTL_IMAGE_OWNER}"
TAG="${CTL_IMAGE_TAG:-latest}"
PREFIX="${REGISTRY}/${OWNER}/code-challenge-ide-pro"

# component (GHCR suffix) -> local tag (DB / ctl defaults)
declare -a PAIRS=(
  "runner-java-25:code-challenge-ide-pro-runner-java-25:local"
  "runner-java-26:code-challenge-ide-pro-runner-java-26:local"
  "runner-python-312:code-challenge-ide-pro-runner-python-312:local"
  "runner-go-123:code-challenge-ide-pro-runner-go-123:local"
  "runner-node-22:code-challenge-ide-pro-runner-node-22:local"
  "runner-dotnet-8:code-challenge-ide-pro-runner-dotnet-8:local"
  "runner-typescript-57:code-challenge-ide-pro-runner-typescript-57:local"
  "runner-rust-184:code-challenge-ide-pro-runner-rust-184:local"
  "runner-cpp-20:code-challenge-ide-pro-runner-cpp-20:local"
  "runner-react-19:code-challenge-ide-pro-runner-react-19:local"
  "runner-vue-35:code-challenge-ide-pro-runner-vue-35:local"
  "runner-angular-19:code-challenge-ide-pro-runner-angular-19:local"
  "runner-postgres-17:code-challenge-ide-pro-runner-postgres-17:local"
  "lsp-java:code-challenge-ide-pro-lsp-java:local"
  "lsp-python:code-challenge-ide-pro-lsp-python:local"
  "lsp-go:code-challenge-ide-pro-lsp-go:local"
  "lsp-typescript:code-challenge-ide-pro-lsp-typescript:local"
  "lsp-dotnet:code-challenge-ide-pro-lsp-dotnet:local"
  "lsp-rust:code-challenge-ide-pro-lsp-rust:local"
  "lsp-cpp:code-challenge-ide-pro-lsp-cpp:local"
)

for pair in "${PAIRS[@]}"; do
  component="${pair%%:*}"
  local_tag="${pair#*:}"
  remote="${PREFIX}-${component}:${TAG}"
  if docker image inspect "${remote}" >/dev/null 2>&1; then
    docker tag "${remote}" "${local_tag}"
    echo "Tagged ${remote} -> ${local_tag}"
  else
    echo "Skip (not pulled): ${remote}" >&2
  fi
done

echo "Local :local tags ready for Ops / Run tests (DB language_runtimes use these names)."
