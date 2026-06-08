#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MAJOR="${1:-26}"
case "${MAJOR}" in
  25) IMAGE="${RUNNER_JAVA_25_IMAGE:-code-challenge-ide-pro-runner-java-25:local}" ;;
  26) IMAGE="${RUNNER_JAVA_26_IMAGE:-code-challenge-ide-pro-runner-java-26:local}" ;;
  *) echo "Unsupported Java major: ${MAJOR} (use 25 or 26)" >&2; exit 1 ;;
esac
docker build -t "${IMAGE}" --build-arg "JAVA_MAJOR=${MAJOR}" \
  -f "${ROOT}/runners/java/Dockerfile" "${ROOT}/runners/java"
echo "Built runner image: ${IMAGE}"
