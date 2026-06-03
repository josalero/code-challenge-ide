#!/usr/bin/env python3
"""Set limits.session_duration_minutes on every challenges/*/challenge.yml (30 easy, 60 otherwise)."""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CHALLENGES_DIR = ROOT / "challenges"


def session_duration_minutes(difficulty: str) -> int:
    return 30 if difficulty.strip().lower() == "easy" else 60


def read_difficulty(text: str, path: Path) -> str:
    match = re.search(r"^difficulty:\s*(\S+)\s*$", text, re.MULTILINE)
    if not match:
        print(f"warn: no difficulty in {path}, defaulting to medium", file=sys.stderr)
        return "medium"
    return match.group(1)


def patch_challenge_yml(path: Path) -> str:
    """Return 'updated', 'unchanged', or 'skipped'."""
    text = path.read_text(encoding="utf-8")
    minutes = session_duration_minutes(read_difficulty(text, path))
    line = f"  session_duration_minutes: {minutes}\n"

    if re.search(r"^\s*session_duration_minutes:\s*\d+\s*$", text, re.MULTILINE):

        def repl(m: re.Match[str]) -> str:
            return f"{m.group(1)}session_duration_minutes: {minutes}"

        new_text = re.sub(
            r"^(\s*)session_duration_minutes:\s*\d+\s*$",
            repl,
            text,
            count=1,
            flags=re.MULTILINE,
        )
        if new_text == text:
            return "unchanged"
        path.write_text(new_text, encoding="utf-8")
        return "updated"

    if "limits:" in text:
        if re.search(r"^\s*per_test_timeout_seconds:\s*\d+\s*$", text, re.MULTILINE):
            new_text, count = re.subn(
                r"(^(\s*)per_test_timeout_seconds:\s*\d+\s*\n)",
                rf"\1\2session_duration_minutes: {minutes}\n",
                text,
                count=1,
                flags=re.MULTILINE,
            )
            if count == 0:
                return "skipped"
        else:
            new_text, count = re.subn(
                r"(^limits:\s*\n)",
                rf"\1{line}",
                text,
                count=1,
                flags=re.MULTILINE,
            )
            if count == 0:
                return "skipped"
    else:
        block = f"limits:\n  per_test_timeout_seconds: 10\n  session_duration_minutes: {minutes}\n"
        for anchor in ("public_tests_meta:", "starter_main_class:", "gating_config:"):
            if anchor in text:
                new_text = text.replace(anchor, block + anchor, 1)
                break
        else:
            new_text = text.rstrip() + "\n" + block

    if new_text == text:
        return "unchanged"
    path.write_text(new_text, encoding="utf-8")
    return "updated"


def main() -> int:
    paths = sorted(CHALLENGES_DIR.glob("*/challenge.yml"))
    if not paths:
        print(f"No challenge.yml under {CHALLENGES_DIR}", file=sys.stderr)
        return 1

    counts = {"updated": 0, "unchanged": 0, "skipped": 0}
    for path in paths:
        result = patch_challenge_yml(path)
        counts[result] = counts.get(result, 0) + 1

    print(
        f"Processed {len(paths)} challenges: "
        f"{counts['updated']} updated, {counts['unchanged']} unchanged, {counts['skipped']} skipped"
    )
    return 0 if counts["skipped"] == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
