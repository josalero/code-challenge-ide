#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IMAGE="${LSP_JAVA_IMAGE:-code-challenge-ide-lsp-java:local}"
docker build -t "${IMAGE}" -f "${ROOT}/runners/lsp-java/Dockerfile" "${ROOT}/runners/lsp-java"
echo "Built LSP image: ${IMAGE}"
