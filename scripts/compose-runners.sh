#!/usr/bin/env bash
# Build runner/LSP images via Compose (no warm — use Admin Ops, make lsp-warm, or compose runner-m2-warm).
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

echo "Building runner images (all languages + LSP)…"
docker compose "${COMPOSE_FILES[@]}" build

echo "Removing orphaned runner compose containers (dropped services, e.g. old Java tracks)…"
for project in code-training-lab-local code-training-lab; do
  docker compose -p "${project}" -f docker-compose.runners.yml down --remove-orphans 2>/dev/null || true
done

echo "Runner images ready."
echo "  Warm Maven cache: Admin Ops → Warm Maven, or:"
echo "    docker compose ${COMPOSE_FILES[*]} run --rm runner-m2-warm"
echo "  Warm LSP: make lsp-warm"
