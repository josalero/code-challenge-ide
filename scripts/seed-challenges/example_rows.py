"""Derive learner-facing input/output example rows from test descriptions and assertions."""

from __future__ import annotations

import json
import re

from challenge_enrichment import SLUG_EXAMPLE_ROWS, normalize_slug
from test_descriptions import describe_java_assert

# Descriptions that are not parseable into input/output.
_VAGUE_DESC = re.compile(
    r"^(verify behavior|verify expected|checks that\b)",
    re.I,
)


def _java_int_array_braced_to_json(braced: str) -> str:
    content = braced.strip().strip("{}").strip()
    if not content:
        return "[]"
    nums = [part.strip() for part in content.split(",") if part.strip()]
    return json.dumps([int(n) for n in nums])


def _java_string_array_braced_to_json(braced: str) -> str:
    content = braced.strip().strip("{}").strip()
    if not content:
        return "[]"
    words = [part.strip().strip('"') for part in content.split(",") if part.strip()]
    return json.dumps(words)


def normalize_call_args(raw: str) -> str:
    """Turn Java call arguments into compact learner-facing values."""
    text = raw.strip()
    if not text:
        return "—"
    parts: list[str] = []
    for segment in _split_top_level_args(text):
        segment = segment.strip()
        int_arr = re.fullmatch(r"new int\[\]\s*(\{[^}]*\})", segment)
        if int_arr:
            parts.append(_java_int_array_braced_to_json(int_arr.group(1)))
            continue
        str_arr = re.fullmatch(r'new String\[\]\s*(\{[^}]*\}|)', segment)
        if str_arr:
            braced = str_arr.group(1) or "{}"
            parts.append(_java_string_array_braced_to_json(braced))
            continue
        parts.append(segment)
    return ", ".join(parts)


def _split_top_level_args(text: str) -> list[str]:
    parts: list[str] = []
    depth = 0
    current: list[str] = []
    for char in text:
        if char in "([{":
            depth += 1
        elif char in ")]}":
            depth -= 1
        if char == "," and depth == 0:
            parts.append("".join(current))
            current = []
            continue
        current.append(char)
    if current:
        parts.append("".join(current))
    return parts


def normalize_output(raw: str) -> str:
    trimmed = raw.strip()
    if trimmed in ("List.of()", "[]"):
        return "[]"
    list_of = re.fullmatch(r"List\.of\((.*)\)", trimmed, re.DOTALL)
    if list_of:
        inner = list_of.group(1).strip()
        if not inner:
            return "[]"
        if inner.startswith("List.of"):
            groups: list[list[str]] = []
            for match in re.finditer(r"List\.of\(([^)]*)\)", inner):
                words = [
                    part.strip().strip('"')
                    for part in match.group(1).split(",")
                    if part.strip()
                ]
                groups.append(words)
            return json.dumps(groups)
        nums = [part.strip() for part in inner.split(",") if part.strip()]
        if all(re.fullmatch(r"-?\d+", n) for n in nums):
            return json.dumps([int(n) for n in nums])
    if re.fullmatch(r"\[[^\]]*\]", trimmed):
        return trimmed.replace(" ", "")
    return trimmed.replace("\n", " ").strip()


def parse_row_from_description(description: str) -> tuple[str, str] | None:
    text = description.strip()
    if not text or _VAGUE_DESC.search(text):
        return None

    patterns: list[tuple[re.Pattern[str], int, int]] = [
        (re.compile(r"^Expect\s+[\w.]+\(new String\[\]\s*(\{[^}]*\})?\)\s+to equal\s+(.+)$", re.I), 1, 2),
        (re.compile(r"^Expect\s+[\w.]+\((.*)\)\s+to equal\s+(.+)$", re.I), 1, 2),
        (re.compile(r"^Expect\s+[\w.]+\((.*)\)\s+to return\s+(.+)$", re.I), 1, 2),
        (re.compile(r"^Expect\s+[\w.]+\((.*)\)\s+to be\s+(true|false)$", re.I), 1, 2),
        (re.compile(r"^[\w.]+\((.*)\)\s+should equal\s+(.+)$", re.I), 1, 2),
        (re.compile(r"^[\w.]+\((.*)\)\s+should return\s+(.+)$", re.I), 1, 2),
        (re.compile(r"^[\w.]+\((.*)\)\s+should return index\s+(-?\d+)$", re.I), 1, 2),
        (re.compile(r"^[\w.]+\((.*)\)\s+should be\s+(true|false)$", re.I), 1, 2),
        (re.compile(r'^reverse(?:String)?\("([^"]*)"\)\s+should be\s+"([^"]*)"$', re.I), 1, 2),
    ]

    gcd = re.match(r"^GCD\((\d+),\s*(\d+)\)\s+should equal\s+(\d+)$", text, re.I)
    if gcd:
        return f"{gcd.group(1)}, {gcd.group(2)}", gcd.group(3)

    for pattern, arg_group, out_group in patterns:
        match = pattern.match(text)
        if not match:
            continue
        args = match.group(arg_group).strip()
        output = match.group(out_group).strip()
        if "new String[]" in args:
            braced = re.search(r"\{([^}]*)\}", args)
            inp = _java_string_array_braced_to_json(f"{{{braced.group(1) if braced else ''}}}")
        else:
            inp = normalize_call_args(args)
        out = normalize_output(output)
        if out_group == 2 and match.re.pattern.endswith(r"(true|false)$"):
            out = out.lower()
        return inp, out

    return None


def is_clear_row(inp: str, out: str) -> bool:
    if not inp or not out or inp == "—" or out == "—":
        return False
    if _VAGUE_DESC.search(inp) or _VAGUE_DESC.search(out):
        return False
    if len(inp) > 120 or len(out) > 160:
        return False
    return True


def rows_from_meta(meta: list[dict[str, str]], slug: str = "", limit: int = 4) -> list[tuple[str, str]]:
    curated = SLUG_EXAMPLE_ROWS.get(slug) or SLUG_EXAMPLE_ROWS.get(normalize_slug(slug))
    if curated:
        return list(curated[:limit])

    rows: list[tuple[str, str]] = []
    seen: set[str] = set()
    for item in meta:
        desc = (item.get("description") or item.get("name") or "").strip()
        parsed = parse_row_from_description(desc)
        if not parsed:
            continue
        inp, out = parsed
        if not is_clear_row(inp, out):
            continue
        key = f"{inp}→{out}"
        if key in seen:
            continue
        seen.add(key)
        rows.append((inp, out))
        if len(rows) >= limit:
            break
    return rows


def rows_from_java_tests(test_bodies: list[str], slug: str, limit: int = 4) -> list[tuple[str, str]]:
    meta = [
        {"description": describe_java_assert(body.strip(), slug)}
        for body in test_bodies
    ]
    return rows_from_meta(meta, slug, limit)


def format_examples_markdown(rows: list[tuple[str, str]]) -> str:
    if not rows:
        return ""
    bullets = [f"- `{inp}` → `{out}`" for inp, out in rows]
    return "\n\n## Examples\n" + "\n".join(bullets)


def strip_examples_section(description: str) -> str:
    """Remove every Examples block (heading or bold) from markdown."""
    base = description.rstrip()
    while True:
        updated = re.sub(
            r"\n##\s+examples?\b[\s\S]*?(?=\n##\s|\n\*\*[A-Z]|\s*$)",
            "",
            base,
            count=1,
            flags=re.I,
        )
        updated = re.sub(
            r"\n\*\*examples?\*\*[\s\S]*?(?=\n\*\*[A-Za-z]|\n##\s|\s*$)",
            "",
            updated,
            count=1,
            flags=re.I,
        )
        if updated == base:
            break
        base = updated
    return re.sub(r"\n{3,}", "\n\n", base).strip()


def upsert_examples_section(description: str, rows: list[tuple[str, str]]) -> str:
    """Insert or replace a ## Examples block in challenge description markdown."""
    section = format_examples_markdown(rows)
    if not section:
        return description

    base = strip_examples_section(description)

    insert_before = re.search(r"\n##\s+constraints\b", base, re.I)
    if insert_before:
        idx = insert_before.start()
        return base[:idx] + section + base[idx:]
    insert_before = re.search(r"\n##\s+method to implement\b", base, re.I)
    if insert_before:
        idx = insert_before.start()
        return base[:idx] + section + base[idx:]
    return base + section
