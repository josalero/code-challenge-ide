"""Resolve Docker images: prefer env override, fall back to local :local tags when GHCR is unavailable."""

from __future__ import annotations

import os
import subprocess


def docker_image_available(image: str) -> bool:
    if not image or not image.strip():
        return False
    try:
        subprocess.run(
            ["docker", "image", "inspect", image.strip()],
            capture_output=True,
            check=True,
            timeout=15,
        )
        return True
    except (subprocess.CalledProcessError, subprocess.TimeoutExpired, OSError):
        return False


def resolve_docker_image(env_key: str, default_image: str) -> str | None:
    """Return the first usable image: explicit env value, then default local tag."""
    explicit = os.environ.get(env_key, "").strip()
    seen: set[str] = set()
    for candidate in (explicit, default_image.strip()):
        if not candidate or candidate in seen:
            continue
        seen.add(candidate)
        if docker_image_available(candidate):
            return candidate
    return None
