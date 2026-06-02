#!/usr/bin/env bash
# Pull runner images from GHCR (production). Tags must match language_runtimes.docker_image.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"
if [[ ! -f .env ]]; then
  echo "Missing .env — run: cp .env.example .env" >&2
  exit 1
fi
set -a
# shellcheck disable=SC1091
source .env
set +a
REGISTRY="${CTL_IMAGE_REGISTRY:-ghcr.io}"
OWNER="${CTL_IMAGE_OWNER:?Set CTL_IMAGE_OWNER in .env}"
TAG="${CTL_IMAGE_TAG:-latest}"
PREFIX="${REGISTRY}/${OWNER}/code-challenge-ide"

COMPONENTS=(
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
  lsp-java
)

for component in "${COMPONENTS[@]}"; do
  echo "Pulling ${PREFIX}-${component}:${TAG}…"
  docker pull "${PREFIX}-${component}:${TAG}"
done

echo "All runner images pulled. Update language_runtimes.docker_image to GHCR tags if not using :local."
