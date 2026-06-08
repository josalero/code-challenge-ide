#!/bin/sh
set -eu

# Named volume at /data/code-challenge-ide-pro-ops is often root-owned on first mount; warm stamps need app (uid 10001).
if [ -d /data/code-challenge-ide-pro-ops ]; then
  chown -R app:app /data/code-challenge-ide-pro-ops
fi

# group_add in compose applies to the entrypoint user (root). Grant app access to the host docker socket GID.
if [ -n "${DOCKER_GID:-}" ] && [ "${DOCKER_GID}" != "0" ]; then
  if getent group "${DOCKER_GID}" >/dev/null 2>&1; then
    GROUP_NAME="$(getent group "${DOCKER_GID}" | cut -d: -f1)"
  else
    GROUP_NAME="hostdocker"
    groupadd -g "${DOCKER_GID}" "${GROUP_NAME}" 2>/dev/null || true
  fi
  usermod -aG "${GROUP_NAME}" app 2>/dev/null || true
fi

exec gosu app java -jar /app/app.jar "$@"
