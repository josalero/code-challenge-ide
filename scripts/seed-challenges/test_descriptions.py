"""Human-readable descriptions for public test cases in generated challenges."""

from __future__ import annotations

import json
import re


def _fmt_bool(value: bool) -> str:
    return "true" if value else "false"


def describe_core_case(slug: str, case: tuple) -> str:
    """Describe a CORE_SPECS public/hidden case tuple."""
    if slug == "gcd":
        return f"GCD({case[0]}, {case[1]}) should equal {case[2]}"
    if slug == "factorial":
        return f"factorial({case[0]}) should equal {case[1]}"
    if slug == "fibonacci-number":
        return f"fibonacci({case[0]}) should equal {case[1]}"
    if slug == "is-prime":
        return f"isPrime({case[0]}) should be {_fmt_bool(case[1])}"
    if slug == "power-of-two":
        return f"isPowerOfTwo({case[0]}) should be {_fmt_bool(case[1])}"
    if slug == "contains-duplicate":
        return f"containsDuplicate({list(case[0])}) should be {_fmt_bool(case[1])}"
    if slug == "missing-number":
        return f"missingNumber({list(case[0])}) should equal {case[1]}"
    if slug == "linear-search":
        return f"linearSearch({list(case[0])}, {case[1]}) should return index {case[2]}"
    if slug == "climbing-stairs":
        return f"climbStairs({case[0]}) should equal {case[1]}"
    if slug == "sqrt-integer":
        return f"integer square root of {case[0]} should be {case[1]}"
    return describe_extended_case(slug, case)


def describe_extended_case(slug: str, case: tuple) -> str:
    """Describe EXTENDED_CORE_SPECS case tuples."""
    if slug == "two-sum":
        return f"twoSum({list(case[0])}, {case[1]}) should return {list(case[2])}"
    if slug == "valid-parentheses":
        return f'isValid("{case[0]}") should be {_fmt_bool(case[1])}'
    if slug == "binary-search":
        return f"binarySearch({list(case[0])}, {case[1]}) should return index {case[2]}"
    if slug == "reverse-string":
        return f'reverseString("{case[0]}") should be "{case[1]}"'
    if slug == "valid-palindrome":
        return f'isPalindrome("{case[0][:20]}…") should be {_fmt_bool(case[1])}' if len(case[0]) > 20 else f'isPalindrome("{case[0]}") should be {_fmt_bool(case[1])}'
    if slug == "max-subarray":
        return f"maxSubArray({list(case[0])}) should equal {case[1]}"
    if slug == "single-number":
        return f"singleNumber({list(case[0])}) should equal {case[1]}"
    if slug == "plus-one":
        return f"plusOne({list(case[0])}) should return {list(case[1])}"
    if slug == "best-time-stock":
        return f"maxProfit({list(case[0])}) should equal {case[1]}"
    if slug == "merge-sorted-arrays":
        return f"mergeSorted({list(case[0])}, {list(case[1])}) should return {list(case[2])}"
    if slug == "bubble-sort":
        return f"bubbleSort({list(case[0])}) should return {list(case[1])}"
    if slug == "anagram-check":
        return f'isAnagram("{case[0]}", "{case[1]}") should be {_fmt_bool(case[2])}'
    return f"case {case!r} should produce {case[-1]!r}"


def _java_string_array_braced_to_json(braced: str) -> str:
    content = braced.strip().strip("{}").strip()
    if not content:
        return "[]"
    words = [part.strip().strip('"') for part in content.split(",") if part.strip()]
    return json.dumps(words)


def _java_list_of_groups_to_json(list_of_body: str) -> str:
    groups: list[list[str]] = []
    for match in re.finditer(r"List\.of\(([^)]*)\)", list_of_body):
        inner = match.group(1)
        words = [part.strip().strip('"') for part in inner.split(",") if part.strip()]
        groups.append(words)
    return json.dumps(groups)


def describe_java_assert(body: str, challenge_slug: str | None = None) -> str:
    """Turn a JUnit assertion line into a short description."""
    text = body.strip().rstrip(";")
    grouped_var = re.search(
        r"expected\s*=\s*List\.of\(([\s\S]*?)\)\s*;\s*assertGroupedEquals\(expected,\s*Solution\.(\w+)\(new String\[\]\s*(\{[^}]*\})?\)\s*\)",
        text,
        re.DOTALL,
    )
    if grouped_var:
        method = grouped_var.group(2)
        braced = grouped_var.group(3) or "{}"
        output_json = _java_list_of_groups_to_json(grouped_var.group(1))
        return f"Expect {method}(new String[] {braced}) to equal {output_json}"
    grouped = re.search(
        r"assertGroupedEquals\(\s*List\.of\(([\s\S]*?)\)\s*,\s*Solution\.(\w+)\(new String\[\]\s*(\{[^}]*\})?\)\s*\)",
        text,
        re.DOTALL,
    )
    if grouped:
        method = grouped.group(2)
        braced = grouped.group(3) or "{}"
        output_json = _java_list_of_groups_to_json(grouped.group(1))
        return f"Expect {method}(new String[] {braced}) to equal {output_json}"
    eq = re.search(
        r"assertEquals\(\s*([^,]+)\s*,\s*Solution\.(\w+)\(([^)]*)\)\s*\)",
        text,
    )
    if eq:
        expected, method, args = eq.group(1), eq.group(2), eq.group(3).strip()
        if expected.strip() == "List.of()":
            expected = "[]"
        if args:
            return f"Expect {method}({args}) to equal {expected}"
        return f"Expect {method}() to equal {expected}"
    arr = re.search(
        r"assertArrayEquals\(\s*new int\[\]\s*\{([^}]+)\}\s*,\s*Solution\.(\w+)\(([^)]*)\)\s*\)",
        text,
    )
    if arr:
        expected, method, args = arr.group(1), arr.group(2), arr.group(3).strip()
        return f"Expect {method}({args}) to return [{expected}]"
    list_eq = re.search(
        r"assertEquals\(\s*List\.of\(([^)]*)\)\s*,\s*Solution\.(\w+)\(([^)]*)\)\s*\)",
        text,
        re.DOTALL,
    )
    if list_eq:
        expected, method, args = list_eq.group(1), list_eq.group(2), list_eq.group(3).strip()
        return f"Expect {method}({args}) to equal List.of({expected})"
    elem = re.search(
        r'assertEquals\(\s*"([^"]+)"\s*,\s*(\w+)\.get\((\d+)\)\s*\)',
        text,
        re.DOTALL,
    )
    if elem:
        expected, var, index = elem.group(1), elem.group(2), elem.group(3)
        call = re.search(r"Solution\.(\w+)\(([^)]*)\)", text)
        if call:
            return f'Expect {call.group(1)}({call.group(2)}) at index {index} to equal "{expected}"'
        return f'Expect {var}[{index}] to equal "{expected}"'
    truth = re.search(
        r"assert(True|False|Equals)\(\s*([^,]+)\s*,\s*Solution\.(\w+)\(([^)]*)\)\s*\)",
        text,
    )
    if truth:
        expected, method, args = truth.group(2), truth.group(3), truth.group(4).strip()
        return f"Expect {method}({args}) to be {expected}"
    if challenge_slug:
        return f"Verify behavior for {challenge_slug}"
    return "Verify expected solution output"


def describe_python_test(func_name: str, body: str) -> str:
    """Describe a pytest test from its body (input/output friendly for the workspace UI)."""
    text = " ".join(body.strip().split())
    equal = re.search(r"assert\s+([\w.]+)\(([^)]*)\)\s*==\s*(.+)$", text)
    if equal:
        return f"Expect {equal.group(1)}({equal.group(2).strip()}) to equal {equal.group(3).strip()}"
    boolean = re.search(r"assert\s+([\w.]+)\(([^)]*)\)\s+is\s+(True|False)", text)
    if boolean:
        return (
            f"Expect {boolean.group(1)}({boolean.group(2).strip()}) "
            f"to be {boolean.group(3).lower()}"
        )
    if func_name.startswith("test_"):
        words = func_name.removeprefix("test_").replace("_", " ")
        return f"Checks that {words}"
    return describe_java_assert(body.replace("assert ", "assertEquals("))


def escape_cpp_string_literal(text: str) -> str:
    """Escape a string for use inside a C++ double-quoted literal."""
    return text.replace("\\", "\\\\").replace('"', '\\"')


def slugify_name(text: str, max_len: int = 48) -> str:
    """Safe identifier fragment for test method/file names."""
    cleaned = re.sub(r"[^a-zA-Z0-9]+", "_", text.lower()).strip("_")
    if not cleaned:
        return "case"
    if cleaned[0].isdigit():
        cleaned = f"c_{cleaned}"
    return cleaned[:max_len]


def meta_entry(name: str, description: str) -> dict[str, str]:
    return {"name": name, "description": description.strip()}
