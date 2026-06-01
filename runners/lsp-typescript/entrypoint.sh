#!/usr/bin/env bash
set -euo pipefail

case "${CTL_LSP_LANGUAGE:-typescript}" in
  vue)
    exec vue-language-server --stdio
    ;;
  *)
    exec typescript-language-server --stdio
    ;;
esac
