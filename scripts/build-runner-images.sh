#!/usr/bin/env bash
# Build Java runner + LSP images and warm the shared Maven cache (Compose).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
exec "${ROOT}/scripts/compose-runners.sh"
