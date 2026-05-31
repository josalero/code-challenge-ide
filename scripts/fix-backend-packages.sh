#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE="${ROOT}/be/src/main/java/com/codetraininglab"

flatten_dir() {
  local nested="$1"
  local parent="$2"
  if [[ -d "${BASE}/${nested}" ]]; then
    shopt -s nullglob
    for f in "${BASE}/${nested}"/*; do
      mv "$f" "${BASE}/${parent}/"
    done
    rmdir "${BASE}/${nested}" 2>/dev/null || true
  fi
}

flatten_dir "platform/config/config" "platform/config"
flatten_dir "platform/security/security" "platform/security"
flatten_dir "platform/persistence/persistence" "platform/persistence"
flatten_dir "integration/runner/runner" "integration/runner"
flatten_dir "integration/lsp/lsp" "integration/lsp"

# platform/web: package is platform.web, not platform.web.api
if [[ -d "${BASE}/platform/web/api" ]]; then
  shopt -s nullglob
  for f in "${BASE}/platform/web/api"/*; do
    mv "$f" "${BASE}/platform/web/"
  done
  rmdir "${BASE}/platform/web/api" 2>/dev/null || true
fi

while IFS= read -r -d '' file; do
  sed -i.bak \
    -e 's/\.application\.application/.application/g' \
    -e 's/\.feedback\.application\.application/.feedback.application/g' \
    "$file"
  rm -f "${file}.bak"
done < <(find "${ROOT}/be/src" -name '*.java' -print0)

echo "Flattened dirs and fixed double application packages."
