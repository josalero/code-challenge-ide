#!/usr/bin/env python3
"""Append public_tests_meta to challenge.yml files that lack it."""

from __future__ import annotations

import re
import sys
from pathlib import Path

from generate import format_public_tests_meta_yaml
from test_descriptions import describe_java_assert, describe_python_test, meta_entry, slugify_name

ROOT = Path(__file__).resolve().parents[2]
CHALLENGES_DIR = ROOT / "challenges"


def humanize_camel(name: str) -> str:
    spaced = re.sub(r"([a-z0-9])([A-Z])", r"\1 \2", name)
    spaced = spaced.replace("_", " ")
    return spaced[:1].upper() + spaced[1:] if spaced else name


def extract_braced_body(text: str, open_brace_index: int) -> str:
    depth = 1
    i = open_brace_index + 1
    while i < len(text) and depth > 0:
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
        i += 1
    return text[open_brace_index + 1 : i - 1]


def extract_java_tests(path: Path) -> list[tuple[str, str]]:
    text = path.read_text(encoding="utf-8")
    results: list[tuple[str, str]] = []
    for match in re.finditer(r"@Test\s+void\s+(\w+)\s*\(\s*\)\s*\{", text):
        name = match.group(1)
        body = extract_braced_body(text, match.end() - 1)
        results.append((name, body))
    return results


def extract_python_tests(path: Path) -> list[tuple[str, str]]:
    text = path.read_text(encoding="utf-8")
    results: list[tuple[str, str]] = []
    for match in re.finditer(r"^def\s+(test_\w+)\s*\([^)]*\)\s*(?:->\s*[^:]+)?\s*:\s*$", text, re.M):
        name = match.group(1)
        start = match.end()
        lines: list[str] = []
        for line in text[start:].splitlines():
            if line and not line[0].isspace() and line.strip():
                break
            if line.strip():
                lines.append(line)
        results.append((name, "\n".join(lines)))
    return results


def extract_node_tests(path: Path) -> list[tuple[str, str]]:
    text = path.read_text(encoding="utf-8")
    results: list[tuple[str, str]] = []
    for match in re.finditer(r'\btest\(\s*"([^"]+)"', text):
        label = match.group(1)
        results.append((slugify_name(label), label[0].upper() + label[1:] if label else label))
    return results


def describe_java_method(slug: str, method: str, body: str) -> str:
    desc = describe_java_assert(body.strip(), slug)
    if desc.startswith("Verify behavior") or desc == "Verify expected solution output":
        return humanize_camel(method)
    return desc


def meta_for_challenge(challenge_dir: Path, language: str) -> list[dict[str, str]]:
    slug = challenge_dir.name
    tests_dir = challenge_dir / "public" / "tests"
    if not tests_dir.is_dir():
        return []

    meta: list[dict[str, str]] = []
    if language == "java":
        for java_file in sorted(tests_dir.glob("*.java")):
            for method, body in extract_java_tests(java_file):
                description = describe_java_method(slug, method, body)
                meta.append(meta_entry(slugify_name(description), description))
    elif language == "python":
        for py_file in sorted(tests_dir.glob("test_*.py")):
            for func, body in extract_python_tests(py_file):
                meta.append(meta_entry(func, describe_python_test(func, body)))
    elif language in ("node", "typescript"):
        pattern = "*.test.js" if language == "node" else "*.test.ts"
        for test_file in sorted(tests_dir.glob(pattern)):
            meta.extend(
                meta_entry(name, desc)
                for name, desc in extract_node_tests(test_file)
            )
    return meta


def insert_meta_into_yml(yml_path: Path, meta: list[dict[str, str]]) -> None:
    block = format_public_tests_meta_yaml(meta).rstrip()
    content = yml_path.read_text(encoding="utf-8")
    if not content.endswith("\n"):
        content += "\n"
    yml_path.write_text(content + block + "\n", encoding="utf-8")


def strip_existing_meta(content: str) -> str:
    lines = content.splitlines()
    out: list[str] = []
    for line in lines:
        if line.startswith("public_tests_meta:"):
            break
        out.append(line)
    text = "\n".join(out)
    return text + ("\n" if text and not text.endswith("\n") else "")


def main() -> int:
    args = [a for a in sys.argv[1:] if a != "--force"]
    force = "--force" in sys.argv
    only_slugs = set(args) if args else None
    updated: list[str] = []
    for yml_path in sorted(CHALLENGES_DIR.glob("*/challenge.yml")):
        if only_slugs and yml_path.parent.name not in only_slugs:
            continue
        content = yml_path.read_text(encoding="utf-8")
        if "public_tests_meta:" in content and not force:
            continue
        if "public_tests_meta:" in content and force:
            content = strip_existing_meta(content)
            yml_path.write_text(content, encoding="utf-8")
        challenge_dir = yml_path.parent
        language = "java"
        for line in content.splitlines():
            if line.startswith("language:"):
                language = line.split(":", 1)[1].strip()
                break
        meta = meta_for_challenge(challenge_dir, language)
        if not meta:
            print(f"skip (no tests): {challenge_dir.name}", file=sys.stderr)
            continue
        insert_meta_into_yml(yml_path, meta)
        updated.append(challenge_dir.name)

    print(f"Updated {len(updated)} challenge(s).")
    for slug in updated:
        print(f"  + {slug}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
