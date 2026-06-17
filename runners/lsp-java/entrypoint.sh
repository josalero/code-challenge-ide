#!/usr/bin/env bash
set -euo pipefail
JDTLS_HOME="${JDTLS_HOME:-/opt/jdtls}"
ARCH="$(uname -m)"
case "${ARCH}" in
  x86_64 | amd64) CONFIG_DIR="${JDTLS_HOME}/config_linux" ;;
  aarch64 | arm64) CONFIG_DIR="${JDTLS_HOME}/config_linux_arm" ;;
  *)
    echo "Unsupported architecture for JDT LS: ${ARCH}" >&2
    exit 1
    ;;
esac
LAUNCHER="$(find "${JDTLS_HOME}" -name 'org.eclipse.equinox.launcher_*.jar' 2>/dev/null | head -1)"
if [[ ! -d "${CONFIG_DIR}" || -z "${LAUNCHER}" ]]; then
  echo "JDT Language Server not installed in image (config=${CONFIG_DIR})" >&2
  exit 1
fi
mkdir -p /workspace /tmp/jdt-data
JDT_DATA_DIR="/tmp/jdt-data/session-$$"
mkdir -p "${JDT_DATA_DIR}"

# JDT LS on JDK 17+ requires --add-modules / --add-opens for reflective access. Without
# them, completion/hover requests can hang silently because JDT cannot inspect java.util
# and java.lang. Heap defaults are also too low for first-time workspace indexing, which
# manifests as the Monaco "Loading…" widget never resolving.
JDT_LOG_LEVEL="${JDT_LOG_LEVEL:-INFO}"
JDT_HEAP_MIN="${JDT_HEAP_MIN:-256m}"
JDT_HEAP_MAX="${JDT_HEAP_MAX:-1g}"

exec java \
  -Declipse.application=org.eclipse.jdt.ls.core.id1 \
  -Dosgi.bundles.defaultStartLevel=4 \
  -Declipse.product=org.eclipse.jdt.ls.core.product \
  -Dlog.level="${JDT_LOG_LEVEL}" \
  -Xms"${JDT_HEAP_MIN}" \
  -Xmx"${JDT_HEAP_MAX}" \
  -XX:+UseG1GC \
  -XX:+HeapDumpOnOutOfMemoryError \
  --add-modules=ALL-SYSTEM \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.io=ALL-UNNAMED \
  -jar "${LAUNCHER}" \
  -configuration "${CONFIG_DIR}" \
  -data "${JDT_DATA_DIR}"
