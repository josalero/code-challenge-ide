#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

build_one() {
  local name="$1"
  local image="$2"
  local context="${ROOT}/runners/lsp-${name}"
  docker build -t "${image}" -f "${context}/Dockerfile" "${context}"
  echo "Built LSP image: ${image}"
}

build_one java "${LSP_JAVA_IMAGE:-code-challenge-ide-pro-lsp-java:local}"
build_one python "${LSP_PYTHON_IMAGE:-code-challenge-ide-pro-lsp-python:local}"
build_one go "${LSP_GO_IMAGE:-code-challenge-ide-pro-lsp-go:local}"
build_one typescript "${LSP_TYPESCRIPT_IMAGE:-code-challenge-ide-pro-lsp-typescript:local}"
build_one dotnet "${LSP_DOTNET_IMAGE:-code-challenge-ide-pro-lsp-dotnet:local}"
build_one rust "${LSP_RUST_IMAGE:-code-challenge-ide-pro-lsp-rust:local}"
build_one cpp "${LSP_CPP_IMAGE:-code-challenge-ide-pro-lsp-cpp:local}"

echo "All LSP images built."
"${ROOT}/scripts/warm-lsp-images.sh" "$@"
