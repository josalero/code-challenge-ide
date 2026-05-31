#!/usr/bin/env bash
# Align Java directory layout with package declarations (bounded-context structure).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAVA="${ROOT}/be/src/main/java/com/codetraininglab"
TEST="${ROOT}/be/src/test/java/com/codetraininglab"

flatten() {
  local nested="$1"
  local parent="$2"
  if [[ -d "${nested}" ]] && [[ "$(ls -A "${nested}" 2>/dev/null)" ]]; then
    mv "${nested}"/*.java "${parent}/"
    rmdir "${nested}" 2>/dev/null || true
  fi
}

flatten "${JAVA}/platform/config/config" "${JAVA}/platform/config"
flatten "${JAVA}/platform/persistence/persistence" "${JAVA}/platform/persistence"
flatten "${JAVA}/platform/security/security" "${JAVA}/platform/security"
flatten "${JAVA}/platform/web/api" "${JAVA}/platform/web"
flatten "${JAVA}/integration/runner/runner" "${JAVA}/integration/runner"
flatten "${JAVA}/integration/lsp/lsp" "${JAVA}/integration/lsp"

fix_packages() {
  local dir="$1"
  find "${dir}" -name '*.java' -print0 | while IFS= read -r -d '' f; do
    sed -i '' \
      -e 's/\.application\.application/.application/g' \
      -e 's/com\.codetraininglab\.submission\.application\.SubmissionEventType/com.codetraininglab.submission.messaging.SubmissionEventType/g' \
      -e 's/com\.codetraininglab\.submission\.application\.SubmissionJobMessage/com.codetraininglab.submission.messaging.SubmissionJobMessage/g' \
      -e 's/com\.codetraininglab\.submission\.application\.SsePayloadKeys/com.codetraininglab.submission.messaging.SsePayloadKeys/g' \
      -e 's/com\.codetraininglab\.api\./com.codetraininglab.platform.web./g' \
      -e 's/com\.codetraininglab\.config\./com.codetraininglab.platform.config./g' \
      -e 's/com\.codetraininglab\.persistence\./com.codetraininglab.platform.persistence./g' \
      -e 's/com\.codetraininglab\.security\./com.codetraininglab.platform.security./g' \
      -e 's/com\.codetraininglab\.runner\./com.codetraininglab.integration.runner./g' \
      -e 's/com\.codetraininglab\.lsp\./com.codetraininglab.integration.lsp./g' \
      -e 's/com\.codetraininglab\.challenge\./com.codetraininglab.catalog./g' \
      -e 's/com\.codetraininglab\.auth\./com.codetraininglab.identity./g' \
      -e 's/com\.codetraininglab\.ai\./com.codetraininglab.coach./g' \
      -e 's/com\.codetraininglab\.ops\./com.codetraininglab.operations./g' \
      -e 's/com\.codetraininglab\.user\./com.codetraininglab.identity./g' \
      "${f}"
  done
}

fix_packages "${JAVA}"
fix_packages "${TEST}"

echo "Package layout fixed under ${JAVA}"
