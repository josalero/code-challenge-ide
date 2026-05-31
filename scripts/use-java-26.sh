#!/usr/bin/env bash
# Enable Java 26 for the current shell session.
# Usage: source scripts/use-java-26.sh
# (Do not execute with ./ — it must run in your current shell.)

set -e

if ! command -v jenv >/dev/null 2>&1; then
  echo "jenv is not installed or not on PATH." >&2
  exit 1
fi

jenv shell 26

echo "Active Java for this shell:"
java -version
