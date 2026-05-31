#!/usr/bin/env bash
set -euo pipefail
JDTLS_HOME="${JDTLS_HOME:-/opt/jdtls}"
CONFIG_DIR="$(find "${JDTLS_HOME}" -maxdepth 4 -type d -name 'config_*' 2>/dev/null | head -1)"
LAUNCHER="$(find "${JDTLS_HOME}" -name 'org.eclipse.equinox.launcher_*.jar' 2>/dev/null | head -1)"
if [[ -z "${CONFIG_DIR}" || -z "${LAUNCHER}" ]]; then
  echo "JDT Language Server not installed in image" >&2
  exit 1
fi
mkdir -p /workspace
exec java \
  -Declipse.application=org.eclipse.jdt.ls.core.id1 \
  -Dosgi.bundles.defaultStartLevel=4 \
  -Declipse.product=org.eclipse.jdt.ls.core.product \
  -Dlog.level=WARN \
  -jar "${LAUNCHER}" \
  -configuration "${CONFIG_DIR}" \
  -data /workspace
