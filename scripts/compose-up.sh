#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

if [[ ! -f .env ]]; then
  echo "Missing .env — run: cp .env.example .env" >&2
  exit 1
fi

if [[ -z "${DOCKER_GID:-}" ]] && [[ -S /var/run/docker.sock ]]; then
  if stat -c '%g' /var/run/docker.sock >/dev/null 2>&1; then
    export DOCKER_GID="$(stat -c '%g' /var/run/docker.sock)"
  elif stat -f '%g' /var/run/docker.sock >/dev/null 2>&1; then
    export DOCKER_GID="$(stat -f '%g' /var/run/docker.sock)"
  fi
  echo "Using DOCKER_GID=${DOCKER_GID:-0} for API container (docker.sock access)"
fi

"${ROOT}/scripts/compose-runners.sh"
docker compose up --build -d --remove-orphans "$@"
echo ""
echo "UI:  http://localhost:${FE_PORT:-3000}"
echo "API: http://localhost:${API_PORT:-8080}/actuator/health"
