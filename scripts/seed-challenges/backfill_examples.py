#!/usr/bin/env python3
"""Inject or refresh ## Examples in challenge.yml from public_tests_meta and tests."""

from __future__ import annotations

import re
import sys
from pathlib import Path

from backfill_public_tests_meta import meta_for_challenge
from challenge_enrichment import build_description_md
from example_rows import rows_from_java_tests, rows_from_meta, upsert_examples_section

ROOT = Path(__file__).resolve().parents[2]
CHALLENGES_DIR = ROOT / "challenges"


def parse_meta_from_yml(content: str) -> list[dict[str, str]]:
    meta: list[dict[str, str]] = []
    in_meta = False
    pending_name: str | None = None
    for line in content.splitlines():
        if line.startswith("public_tests_meta:"):
            in_meta = True
            continue
        if not in_meta:
            continue
        name_match = re.match(r'\s*-\s*name:\s*"(.*)"\s*$', line)
        if name_match:
            pending_name = name_match.group(1)
            continue
        desc_match = re.match(r'\s*description:\s*"(.*)"\s*$', line)
        if desc_match and pending_name:
            meta.append({"name": pending_name, "description": desc_match.group(1)})
            pending_name = None
    return meta


def extract_description_md(content: str) -> str:
    lines = content.splitlines()
    out: list[str] = []
    in_desc = False
    for line in lines:
        if line.startswith("description_md:"):
            in_desc = True
            if line.strip() != "description_md: |":
                out.append(line.split("description_md:", 1)[1].strip())
            continue
        if in_desc:
            if line.startswith("  "):
                out.append(line[2:])
            elif line.strip() == "":
                out.append("")
            else:
                break
    return "\n".join(out).strip()


def replace_description_md(content: str, new_desc: str) -> str:
    lines = content.splitlines()
    out: list[str] = []
    i = 0
    while i < len(lines):
        if lines[i].startswith("description_md:"):
            out.append("description_md: |")
            for line in new_desc.splitlines():
                out.append(f"  {line}")
            i += 1
            while i < len(lines) and (lines[i].startswith("  ") or lines[i].strip() == ""):
                i += 1
            continue
        out.append(lines[i])
        i += 1
    text = "\n".join(out)
    return text + ("\n" if not text.endswith("\n") else "")


def read_field(content: str, key: str, default: str = "") -> str:
    for line in content.splitlines():
        if line.startswith(f"{key}:"):
            return line.split(":", 1)[1].strip().strip('"')
    return default


def collect_java_bodies(challenge_dir: Path) -> list[str]:
    from backfill_public_tests_meta import extract_java_tests

    bodies: list[str] = []
    tests_dir = challenge_dir / "public" / "tests"
    if not tests_dir.is_dir():
        return bodies
    for java_file in sorted(tests_dir.glob("*.java")):
        for _method, body in extract_java_tests(java_file):
            bodies.append(body)
    return bodies


def enrich_challenge(challenge_dir: Path, force: bool = False) -> bool:
    yml_path = challenge_dir / "challenge.yml"
    content = yml_path.read_text(encoding="utf-8")
    slug = challenge_dir.name
    has_examples = bool(
        re.search(r"^##\s+examples?\b", content, re.I | re.M)
        or re.search(r"\*\*examples?\*\*", content, re.I)
    )
    duplicate_examples = len(re.findall(r"^##\s+examples?\b", content, re.I | re.M)) > 1
    if has_examples and not force and not duplicate_examples:
        return False

    language = read_field(content, "language", "java")
    meta = parse_meta_from_yml(content)
    if not meta:
        meta = meta_for_challenge(challenge_dir, language)

    rows = rows_from_meta(meta, slug)
    if not rows and language == "java":
        rows = rows_from_java_tests(collect_java_bodies(challenge_dir), slug)

    if not rows:
        return False

    description = extract_description_md(content)
    if duplicate_examples or (re.search(r"^##\s+what to do\b", description, re.I | re.M) and force):
        from example_rows import strip_examples_section

        description = strip_examples_section(description)

    if re.search(r"^##\s+what to do\b", description, re.I | re.M):
        updated = upsert_examples_section(description, rows)
    elif duplicate_examples:
        updated = upsert_examples_section(description, rows)
    else:
        # Only enrich structure when the file still has a short plain description.
        base = description
        updated = build_description_md(
            slug,
            read_field(content, "title", slug.replace("-", " ").title()),
            read_field(content, "difficulty", "easy"),
            language,
            base,
            meta,
        )

    yml_path.write_text(replace_description_md(content, updated), encoding="utf-8")
    return True


def main() -> int:
    force = "--force" in sys.argv
    only = {a for a in sys.argv[1:] if not a.startswith("--")}
    updated: list[str] = []
    for yml_path in sorted(CHALLENGES_DIR.glob("*/challenge.yml")):
        slug = yml_path.parent.name
        if only and slug not in only:
            continue
        if enrich_challenge(yml_path.parent, force=force):
            updated.append(slug)
    print(f"Updated examples for {len(updated)} challenge(s).")
    for slug in updated[:40]:
        print(f"  + {slug}")
    if len(updated) > 40:
        print(f"  ... and {len(updated) - 40} more")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
