#!/usr/bin/env bash
# Pull Java runner images from GHCR (production). Tags must match language_runtimes.docker_image.
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
for major in 17 21 25 26; do
  docker pull "${REGISTRY}/${OWNER}/code-challenge-ide-runner-java-${major}:${TAG}"
done
docker pull "${REGISTRY}/${OWNER}/code-challenge-ide-lsp-java:${TAG}"
