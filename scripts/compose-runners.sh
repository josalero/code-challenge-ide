#!/usr/bin/env bash
# Build runner/LSP images via Compose and warm the shared Maven cache volume.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

COMPOSE_FILES=(-f docker-compose.runners.yml)
if [[ -f docker-compose.local.yml ]]; then
  COMPOSE_FILES=(-f docker-compose.local.yml -f docker-compose.runners.yml)
fi

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

echo "Building runner images (Java 17/21/25/26, Python 3.12, LSP)…"
docker compose "${COMPOSE_FILES[@]}" build

echo "Warming Maven cache volume (ctl-runner-m2-cache)…"
docker compose "${COMPOSE_FILES[@]}" run --rm runner-m2-warm

echo "Runner images ready. Set RUNNER_MAVEN_CACHE_VOLUME=ctl-runner-m2-cache in .env for faster runs."
