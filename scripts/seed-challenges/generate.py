#!/usr/bin/env python3
"""Generate challenge directories from catalog modules. Skips existing slugs."""

from __future__ import annotations

import re
import sys
from pathlib import Path

from catalog import JAVA_CHALLENGES, PYTHON_CHALLENGES
from test_descriptions import (
    describe_core_case,
    describe_java_assert,
    describe_python_test,
    meta_entry,
    slugify_name,
)
from catalog_frontend import FRONTEND_CHALLENGES
from catalog_frontend_extra import FRONTEND_EXTRA_CHALLENGES
from catalog_multi import (
    CPP_CHALLENGES,
    CSHARP_CHALLENGES,
    GO_CHALLENGES,
    NODE_CHALLENGES,
    RUST_CHALLENGES,
    TYPESCRIPT_CHALLENGES,
)
from catalog_multi_extended import (
    EXTENDED_CPP_CHALLENGES,
    EXTENDED_CSHARP_CHALLENGES,
    EXTENDED_GO_CHALLENGES,
    EXTENDED_NODE_CHALLENGES,
    EXTENDED_RUST_CHALLENGES,
    EXTENDED_TYPESCRIPT_CHALLENGES,
)
from catalog_typescript_extra import TYPESCRIPT_EXTRA_CHALLENGES

ROOT = Path(__file__).resolve().parents[2]
CHALLENGES_DIR = ROOT / "challenges"

JAVA_TEST_HEADER = """package com.challenge.{package};

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
"""

PYTHON_TEST_HEADER = "from solution import *\n\n"

LANG_CONFIG = {
    "java": {"starter_name": "Solution.java", "starter_main": "com.challenge.Solution"},
    "python": {"starter_name": "solution.py", "starter_main": "solution"},
    "go": {"starter_name": "solution.go"},
    "node": {"starter_name": "solution.js"},
    "typescript": {"starter_name": "solution.ts"},
    "csharp": {"starter_name": "Solution.cs"},
    "rust": {"starter_name": "lib.rs"},
    "cpp": {"starter_name": "solution.cpp"},
    "react": {"starter_name": "solution.tsx"},
    "vue": {"starter_name": "solution.vue"},
    "angular": {"starter_name": "solution.ts"},
}


def java_test_class(package: str, class_name: str, cases: list[tuple[str, str]]) -> str:
    parts = [JAVA_TEST_HEADER.format(package=package), f"class {class_name} {{\n"]
    for method_name, body in cases:
        indented = "\n        ".join(line.strip() for line in body.strip().splitlines() if line.strip())
        parts.append(f"\n    @Test\n    void {method_name}() {{\n        {indented}\n    }}\n")
    parts.append("}\n")
    return "".join(parts)


def group_java_tests(
    tests: list[tuple[str, str]], slug: str
) -> dict[str, list[tuple[str, str]]]:
    grouped: dict[str, list[tuple[str, str]]] = {}
    used_methods: set[str] = set()
    for class_name, body in tests:
        method = slugify_name(describe_java_assert(body, slug))
        base = method
        n = 2
        while method in used_methods:
            method = f"{base}_{n}"
            n += 1
        used_methods.add(method)
        grouped.setdefault(class_name, []).append((method, body))
    return grouped


def format_public_tests_meta_yaml(meta: list[dict[str, str]]) -> str:
    if not meta:
        return ""
    lines = ["public_tests_meta:"]
    for item in meta:
        name = item["name"].replace('"', '\\"')
        desc = item["description"].replace('"', '\\"')
        lines.append(f'  - name: "{name}"')
        lines.append(f'    description: "{desc}"')
    return "\n".join(lines) + "\n"


def java_public_meta(entry: dict) -> list[dict[str, str]]:
    slug = entry["slug"]
    result: list[dict[str, str]] = []
    seen: set[str] = set()
    for _class_name, body in entry.get("public_tests", []):
        description = describe_java_assert(body, slug)
        method = slugify_name(description)
        base = method
        n = 2
        while method in seen:
            method = f"{base}_{n}"
            n += 1
        seen.add(method)
        result.append(meta_entry(method, description))
    return result


def python_public_meta(entry: dict) -> list[dict[str, str]]:
    result: list[dict[str, str]] = []
    for func_name, body in entry.get("public_tests", []):
        result.append(meta_entry(func_name, describe_python_test(func_name, body)))
    return result


def frontend_public_meta(entry: dict) -> list[dict[str, str]]:
    result: list[dict[str, str]] = []
    for _name, source in entry.get("public_tests", []):
        for match in re.finditer(r'\bit\(\s*"([^"]+)"', source):
            label = match.group(1)
            result.append(
                meta_entry(
                    slugify_name(label),
                    label[0].upper() + label[1:] if label else label,
                )
            )
    return result


def python_test_file(func_name: str, body: str) -> str:
    return PYTHON_TEST_HEADER + f"def {func_name}() -> None:\n    {body.strip()}\n\n"


def write_java_challenge(entry: dict, base: Path) -> None:
    slug = entry["slug"]
    challenge_dir = base / slug
    challenge_dir.mkdir(parents=True, exist_ok=True)

    yml = f"""slug: {slug}
title: {entry["title"]}
difficulty: {entry["difficulty"]}
language: java
default_runtime_version: "26"
description_md: |
  {entry["description"].replace(chr(10), chr(10) + "  ")}
gating_config:
  line_coverage_percent: 80
  checkstyle_max_errors: 0
limits:
  per_test_timeout_seconds: 10
starter_main_class: com.challenge.Solution
{format_public_tests_meta_yaml(java_public_meta(entry))}"""
    (challenge_dir / "challenge.yml").write_text(yml, encoding="utf-8")

    starter_dir = challenge_dir / "starter"
    starter_dir.mkdir(exist_ok=True)
    (starter_dir / "Solution.java").write_text(entry["starter"].strip() + "\n", encoding="utf-8")

    for scope, package in [("public", "public_"), ("hidden", "hidden")]:
        tests_dir = challenge_dir / scope / "tests"
        tests_dir.mkdir(parents=True, exist_ok=True)
        tests_key = "public_tests" if scope == "public" else "hidden_tests"
        for class_name, cases in group_java_tests(entry[tests_key], slug).items():
            content = java_test_class(package, class_name, cases)
            (tests_dir / f"{class_name}.java").write_text(content, encoding="utf-8")


def write_python_challenge(entry: dict, base: Path) -> None:
    slug = entry["slug"]
    challenge_dir = base / slug
    challenge_dir.mkdir(parents=True, exist_ok=True)

    yml = f"""slug: {slug}
title: {entry["title"]}
difficulty: {entry["difficulty"]}
language: python
default_runtime_version: "3.12"
description_md: |
  {entry["description"].replace(chr(10), chr(10) + "  ")}
gating_config:
  line_coverage_percent: 80
limits:
  per_test_timeout_seconds: 10
starter_main_class: solution
{format_public_tests_meta_yaml(python_public_meta(entry))}"""
    (challenge_dir / "challenge.yml").write_text(yml, encoding="utf-8")

    starter_dir = challenge_dir / "starter"
    starter_dir.mkdir(exist_ok=True)
    (starter_dir / "solution.py").write_text(entry["starter"].strip() + "\n", encoding="utf-8")

    for scope in ("public", "hidden"):
        tests_dir = challenge_dir / scope / "tests"
        tests_dir.mkdir(parents=True, exist_ok=True)
        tests_key = "public_tests" if scope == "public" else "hidden_tests"
        for func_name, body in entry[tests_key]:
            content = python_test_file(func_name, body)
            (tests_dir / f"{func_name}.py").write_text(content, encoding="utf-8")


def write_generic_challenge(entry: dict, base: Path) -> None:
    lang = entry["language"]
    cfg = LANG_CONFIG[lang]
    slug = entry["slug"]
    challenge_dir = base / slug
    challenge_dir.mkdir(parents=True, exist_ok=True)

    yml = f"""slug: {slug}
title: {entry["title"]}
difficulty: {entry["difficulty"]}
language: {lang}
default_runtime_version: "{entry["runtime"]}"
description_md: |
  {entry["description"].replace(chr(10), chr(10) + "  ")}
gating_config:
  line_coverage_percent: 80
  checkstyle_max_errors: 0
limits:
  per_test_timeout_seconds: 10
{format_public_tests_meta_yaml(entry.get("public_tests_meta") or frontend_public_meta(entry))}"""
    (challenge_dir / "challenge.yml").write_text(yml, encoding="utf-8")

    starter_rel = cfg.get("starter_dir", "starter")
    starter_dir = challenge_dir / starter_rel
    starter_dir.mkdir(parents=True, exist_ok=True)
    (starter_dir / cfg["starter_name"]).write_text(entry["starter"].strip() + "\n", encoding="utf-8")

    for scope in ("public", "hidden"):
        tests_dir = challenge_dir / scope / "tests"
        tests_dir.mkdir(parents=True, exist_ok=True)
        tests_key = "public_tests" if scope == "public" else "hidden_tests"
        for name, source in entry[tests_key]:
            fname = name if "." in name else name
            if lang == "go" and not fname.endswith("_test.go"):
                fname = fname + "_test.go"
            elif lang == "node" and not fname.endswith(".test.js"):
                fname = fname + ".test.js"
            elif lang == "typescript" and not fname.endswith(".test.ts"):
                fname = fname + ".test.ts"
            elif lang == "csharp" and not fname.endswith(".cs"):
                fname = fname + ".cs"
            elif lang == "rust" and not fname.endswith(".rs"):
                fname = fname + ".rs"
            elif lang == "cpp" and not fname.endswith(".cpp"):
                fname = fname + ".cpp"
            elif lang == "react" and not fname.endswith(".test.tsx"):
                fname = (fname + ".test.tsx").replace(".test.test.tsx", ".test.tsx")
            elif lang == "vue" and not fname.endswith(".test.ts"):
                fname = fname + ".test.ts"
            elif lang == "angular" and not fname.endswith(".test.ts"):
                fname = fname + ".test.ts"
            (tests_dir / fname).write_text(source.strip() + "\n", encoding="utf-8")


def process_entries(entries: list[dict], writer, base: Path, force: bool) -> tuple[list[str], list[str]]:
    created: list[str] = []
    skipped: list[str] = []
    for entry in entries:
        slug = entry["slug"]
        target = base / slug
        if target.exists() and not force:
            skipped.append(slug)
            continue
        writer(entry, base)
        created.append(slug)
    return created, skipped


def main() -> int:
    force = "--force" in sys.argv
    all_created: list[str] = []
    all_skipped: list[str] = []

    all_frontend = FRONTEND_CHALLENGES + FRONTEND_EXTRA_CHALLENGES
    all_typescript = (
        TYPESCRIPT_CHALLENGES
        + EXTENDED_TYPESCRIPT_CHALLENGES
        + TYPESCRIPT_EXTRA_CHALLENGES
    )

    batches = [
        (JAVA_CHALLENGES, write_java_challenge),
        (PYTHON_CHALLENGES, write_python_challenge),
        (GO_CHALLENGES + EXTENDED_GO_CHALLENGES, write_generic_challenge),
        (NODE_CHALLENGES + EXTENDED_NODE_CHALLENGES, write_generic_challenge),
        (all_typescript, write_generic_challenge),
        (CSHARP_CHALLENGES + EXTENDED_CSHARP_CHALLENGES, write_generic_challenge),
        (RUST_CHALLENGES + EXTENDED_RUST_CHALLENGES, write_generic_challenge),
        (CPP_CHALLENGES + EXTENDED_CPP_CHALLENGES, write_generic_challenge),
        (all_frontend, write_generic_challenge),
    ]

    for entries, writer in batches:
        created, skipped = process_entries(entries, writer, CHALLENGES_DIR, force)
        all_created.extend(created)
        all_skipped.extend(skipped)

    print(f"Created {len(all_created)} challenge(s).")
    for s in all_created:
        print(f"  + {s}")
    if all_skipped:
        print(f"Skipped {len(all_skipped)} existing slug(s).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
