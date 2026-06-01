"""
Multi-language challenge catalog (Go, Node.js, TypeScript, C#, Rust).
Generated from shared CORE_SPECS — mirrors classic DSA exercises in JAVA_CHALLENGES.
"""

import json

from test_descriptions import describe_core_case, meta_entry, slugify_name

CORE_SPECS = [
    {
        "slug": "factorial",
        "title": "Factorial",
        "difficulty": "easy",
        "description": "Return n! for n >= 0 (0! = 1).",
        "public": [(0, 1), (5, 120), (1, 1)],
        "hidden": [(10, 3628800), (3, 6)],
    },
    {
        "slug": "fibonacci-number",
        "title": "Fibonacci Number",
        "difficulty": "easy",
        "description": "Return the n-th Fibonacci number (fib(0)=0, fib(1)=1).",
        "public": [(0, 0), (1, 1), (5, 5)],
        "hidden": [(10, 55), (6, 8)],
    },
    {
        "slug": "gcd",
        "title": "Greatest Common Divisor",
        "difficulty": "easy",
        "description": "Return the greatest common divisor of a and b.",
        "public": [(54, 24, 6), (17, 13, 1), (25, 15, 5)],
        "hidden": [(48, 18, 12), (0, 7, 7)],
    },
    {
        "slug": "is-prime",
        "title": "Prime Check",
        "difficulty": "easy",
        "description": "Return true if n >= 2 is prime.",
        "public": [(1, False), (2, True), (17, True)],
        "hidden": [(15, False), (97, True)],
    },
    {
        "slug": "contains-duplicate",
        "title": "Contains Duplicate",
        "difficulty": "easy",
        "description": "Return true if any value appears at least twice.",
        "public": [([1, 2, 3, 1], True), ([1, 2, 3, 4], False)],
        "hidden": [([1, 1], True), ([], False)],
    },
    {
        "slug": "missing-number",
        "title": "Missing Number",
        "difficulty": "easy",
        "description": "Array contains n distinct values in [0,n]; one missing. Return it.",
        "public": [([3, 0, 1], 2), ([0], 1)],
        "hidden": [([9, 6, 4, 2, 3, 5, 7, 0, 1], 8), ([1], 0)],
    },
    {
        "slug": "climbing-stairs",
        "title": "Climbing Stairs",
        "difficulty": "easy",
        "description": "Count ways to climb n steps (1 or 2 at a time).",
        "public": [(2, 2), (3, 3)],
        "hidden": [(10, 89), (1, 1)],
    },
    {
        "slug": "power-of-two",
        "title": "Power of Two",
        "difficulty": "easy",
        "description": "Return true if n is a power of two (n > 0).",
        "public": [(1, True), (3, False)],
        "hidden": [(16, True), (0, False)],
    },
    {
        "slug": "linear-search",
        "title": "Linear Search",
        "difficulty": "easy",
        "description": "Return index of target in nums, or -1.",
        "public": [([2, 3, 4], 3, 1), ([1, 2], 5, -1)],
        "hidden": [([9], 9, 0), ([], 1, -1)],
    },
    {
        "slug": "sqrt-integer",
        "title": "Sqrt(x)",
        "difficulty": "easy",
        "description": "Return integer square root (floor).",
        "public": [(8, 2), (0, 0)],
        "hidden": [(10, 3), (2147483647, 46340)],
    },
]


def _fn_name(slug: str) -> str:
    return slug.replace("-", "_")


def _go_slice(arr: list) -> str:
    return "[]int{" + ", ".join(str(x) for x in arr) + "}"


def build_go(spec: dict) -> dict:
    if spec["slug"] == "gcd":
        sig, call = "func Gcd(a, b int) int", "Gcd"
    elif spec["slug"] == "is-prime":
        sig, call = "func IsPrime(n int) bool", "IsPrime"
    elif spec["slug"] == "contains-duplicate":
        sig, call = "func ContainsDuplicate(nums []int) bool", "ContainsDuplicate"
    elif spec["slug"] == "missing-number":
        sig, call = "func MissingNumber(nums []int) int", "MissingNumber"
    elif spec["slug"] == "linear-search":
        sig, call = "func LinearSearch(nums []int, target int) int", "LinearSearch"
    elif spec["slug"] == "sqrt-integer":
        sig, call = "func MySqrt(x int) int", "MySqrt"
    elif spec["slug"] == "power-of-two":
        sig, call = "func IsPowerOfTwo(n int) bool", "IsPowerOfTwo"
    elif spec["slug"] == "climbing-stairs":
        sig, call = "func ClimbStairs(n int) int", "ClimbStairs"
    elif spec["slug"] == "fibonacci-number":
        sig, call = "func Fib(n int) int", "Fib"
    else:
        sig, call = "func Factorial(n int) int", "Factorial"

    starter = f"package solution\n\n{sig} {{\n\tpanic(\"TODO\")\n}}\n"

    def go_assert(case) -> str:
        if spec["slug"] == "linear-search":
            args = f"{_go_slice(case[0])}, {case[1]}"
            want = str(case[2])
        elif spec["slug"] in ("contains-duplicate", "missing-number"):
            args = _go_slice(case[0])
            want = "true" if case[1] is True else "false" if case[1] is False else str(case[1])
        elif spec["slug"] in ("is-prime", "power-of-two"):
            args = str(case[0])
            want = "true" if case[1] else "false"
        else:
            args = ", ".join(str(x) for x in case[:-1])
            want = str(case[-1])
        return f"if solution.{call}({args}) != {want} {{ t.Fatal(\"unexpected\") }}"

    def go_test_file(cases: list, prefix: str) -> list[tuple[str, str]]:
        files: list[tuple[str, str]] = []
        for i, case in enumerate(cases):
            desc = describe_core_case(spec["slug"], case)
            test_id = slugify_name(f"{prefix}_{desc}")
            fn_name = "".join(part.capitalize() for part in test_id.split("_"))
            body = go_assert(case)
            src = f"""package solution_test

import (
	"testing"

	"challenge/solution"
)

func Test{fn_name}(t *testing.T) {{
	{body}
}}
"""
            files.append((f"{test_id}_test", src))
        return files

    return {
        "slug": f"{spec['slug']}-go",
        "title": f"{spec['title']} (Go)",
        "difficulty": spec["difficulty"],
        "description": spec["description"],
        "language": "go",
        "runtime": "1.23",
        "starter": starter,
        "public_tests": go_test_file(spec["public"], "public"),
        "hidden_tests": go_test_file(spec["hidden"], "hidden"),
        "public_tests_meta": [
            meta_entry(slugify_name(describe_core_case(spec["slug"], c)), describe_core_case(spec["slug"], c))
            for c in spec["public"]
        ],
    }


def build_node(spec: dict) -> dict:
    if spec["slug"] == "gcd":
        js_fn = "gcd"
    elif spec["slug"] == "is-prime":
        js_fn = "isPrime"
    elif spec["slug"] == "contains-duplicate":
        js_fn = "containsDuplicate"
    elif spec["slug"] == "missing-number":
        js_fn = "missingNumber"
    elif spec["slug"] == "linear-search":
        js_fn = "linearSearch"
    elif spec["slug"] == "sqrt-integer":
        js_fn = "mySqrt"
    elif spec["slug"] == "power-of-two":
        js_fn = "isPowerOfTwo"
    elif spec["slug"] == "climbing-stairs":
        js_fn = "climbStairs"
    elif spec["slug"] == "fibonacci-number":
        js_fn = "fib"
    else:
        js_fn = _fn_name(spec["slug"])

    starter = f"""function {js_fn}(/* args */) {{
  throw new Error("TODO");
}}

module.exports = {{ {js_fn} }};
"""

    def js_call(case) -> str:
        if spec["slug"] == "linear-search":
            args = f"{json.dumps(case[0])}, {case[1]}"
            want = str(case[2])
        elif spec["slug"] in ("contains-duplicate", "missing-number"):
            args = json.dumps(case[0])
            want = "true" if case[1] is True else "false" if case[1] is False else str(case[1])
        elif spec["slug"] in ("is-prime", "power-of-two"):
            args = str(case[0])
            want = "true" if case[1] else "false"
        else:
            args = ", ".join(str(x) for x in case[:-1])
            want = str(case[-1])
        return f"assert.equal({js_fn}({args}), {want});"

    def node_test_file(cases: list, prefix: str) -> list[tuple[str, str]]:
        files: list[tuple[str, str]] = []
        for i, case in enumerate(cases):
            desc = describe_core_case(spec["slug"], case)
            test_id = slugify_name(f"{prefix}_{i + 1}")
            src = f"""const {{ test }} = require("node:test");
const assert = require("node:assert/strict");
const {{ {js_fn} }} = require("../solution.js");

test("{desc}", () => {{
  {js_call(case)}
}});
"""
            files.append((test_id, src))
        return files

    return {
        "slug": f"{spec['slug']}-node",
        "title": f"{spec['title']} (Node.js)",
        "difficulty": spec["difficulty"],
        "description": spec["description"],
        "language": "node",
        "runtime": "22",
        "starter": starter,
        "public_tests": node_test_file(spec["public"], "public"),
        "hidden_tests": node_test_file(spec["hidden"], "hidden"),
        "public_tests_meta": [
            meta_entry(slugify_name(describe_core_case(spec["slug"], c)), describe_core_case(spec["slug"], c))
            for i, c in enumerate(spec["public"])
        ],
    }


def build_typescript(spec: dict) -> dict:
    node = build_node(spec)
    js_fn = node["starter"].split("function ")[1].split("(")[0]
    entry = {
        "slug": f"{spec['slug']}-typescript",
        "title": f"{spec['title']} (TypeScript)",
        "difficulty": spec["difficulty"],
        "description": spec["description"],
        "language": "typescript",
        "runtime": "5.7",
    }
    params = "n: number"
    if spec["slug"] == "gcd":
        params = "a: number, b: number"
    elif spec["slug"] in ("contains-duplicate", "missing-number"):
        params = "nums: number[]"
    elif spec["slug"] == "linear-search":
        params = "nums: number[], target: number"
    ret = "boolean" if spec["slug"] in ("is-prime", "power-of-two", "contains-duplicate") else "number"
    entry["starter"] = f"export function {js_fn}({params}): {ret} {{\n  throw new Error('TODO');\n}}\n"
    def ts_call(case) -> str:
        if spec["slug"] == "linear-search":
            args = f"{json.dumps(case[0])}, {case[1]}"
            want = str(case[2])
        elif spec["slug"] in ("contains-duplicate", "missing-number"):
            args = json.dumps(case[0])
            want = "true" if case[1] is True else "false" if case[1] is False else str(case[1])
        elif spec["slug"] in ("is-prime", "power-of-two"):
            args = str(case[0])
            want = "true" if case[1] else "false"
        else:
            args = ", ".join(str(x) for x in case[:-1])
            want = str(case[-1])
        return f"assert.equal({js_fn}({args}), {want});"

    def ts_test_file(cases: list, prefix: str) -> list[tuple[str, str]]:
        files: list[tuple[str, str]] = []
        for i, case in enumerate(cases):
            desc = describe_core_case(spec["slug"], case)
            test_id = slugify_name(f"{prefix}_{i + 1}")
            src = f"""import {{ test }} from "node:test";
import assert from "node:assert/strict";
import {{ {js_fn} }} from "../solution.ts";

test("{desc}", () => {{
  {ts_call(case)}
}});
"""
            files.append((test_id, src))
        return files

    entry["public_tests"] = ts_test_file(spec["public"], "public")
    entry["hidden_tests"] = ts_test_file(spec["hidden"], "hidden")
    entry["public_tests_meta"] = [
        meta_entry(slugify_name(describe_core_case(spec["slug"], c)), describe_core_case(spec["slug"], c))
        for i, c in enumerate(spec["public"])
    ]
    return entry


def build_csharp(spec: dict) -> dict:
    fn_pascal = "".join(p.capitalize() for p in spec["slug"].split("-"))
    if spec["slug"] == "fibonacci-number":
        fn_pascal = "Fib"
    elif spec["slug"] == "gcd":
        fn_pascal = "Gcd"
    elif spec["slug"] == "is-prime":
        fn_pascal = "IsPrime"
    elif spec["slug"] == "contains-duplicate":
        fn_pascal = "ContainsDuplicate"
    elif spec["slug"] == "missing-number":
        fn_pascal = "MissingNumber"
    elif spec["slug"] == "linear-search":
        fn_pascal = "LinearSearch"
    elif spec["slug"] == "sqrt-integer":
        fn_pascal = "MySqrt"
    elif spec["slug"] == "power-of-two":
        fn_pascal = "IsPowerOfTwo"
    elif spec["slug"] == "climbing-stairs":
        fn_pascal = "ClimbStairs"
    elif spec["slug"] == "factorial":
        fn_pascal = "Factorial"

    ret = "bool" if spec["slug"] in ("is-prime", "power-of-two", "contains-duplicate") else "int"
    params = "int n"
    if spec["slug"] == "gcd":
        params = "int a, int b"
    elif spec["slug"] in ("contains-duplicate", "missing-number"):
        params = "int[] nums"
    elif spec["slug"] == "linear-search":
        params = "int[] nums, int target"

    starter = f"""namespace Challenge;

public static class Solution
{{
    public static {ret} {fn_pascal}({params})
    {{
        throw new NotImplementedException();
    }}
}}
"""

    def cs_assert(case) -> str:
        if spec["slug"] == "linear-search":
            arr = "new int[] { " + ", ".join(str(x) for x in case[0]) + " }"
            args = f"{arr}, {case[1]}"
            want = str(case[2])
        elif spec["slug"] in ("contains-duplicate", "missing-number"):
            arr = "new int[] { " + ", ".join(str(x) for x in case[0]) + " }"
            args = arr
            want = "true" if case[1] is True else "false"
        elif spec["slug"] in ("is-prime", "power-of-two"):
            args = str(case[0])
            want = "true" if case[1] else "false"
        else:
            args = ", ".join(str(x) for x in case[:-1])
            want = str(case[-1]).lower() if isinstance(case[-1], bool) else str(case[-1])
        return f"Assert.Equal({want}, Solution.{fn_pascal}({args}));"

    def cs_method(case) -> tuple[str, str]:
        desc = describe_core_case(spec["slug"], case)
        method = "".join(part.capitalize() for part in slugify_name(desc).split("_"))
        return method, cs_assert(case)

    pub_methods = "\n\n    ".join(
        f'[Fact]\n    public void {name}()\n    {{\n        {body}\n    }}'
        for name, body in (cs_method(c) for c in spec["public"])
    )
    hid_methods = "\n\n    ".join(
        f'[Fact]\n    public void {name}()\n    {{\n        {body}\n    }}'
        for name, body in (cs_method(c) for c in spec["hidden"])
    )
    pub_test = f"""using Challenge;
using Xunit;

namespace Challenge.Tests;

public class PublicTests
{{
    {pub_methods}
}}
"""
    hid_test = f"""using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{{
    {hid_methods}
}}
"""
    return {
        "slug": f"{spec['slug']}-csharp",
        "title": f"{spec['title']} (C#)",
        "difficulty": spec["difficulty"],
        "description": spec["description"],
        "language": "csharp",
        "runtime": "8.0",
        "starter": starter,
        "public_tests": [("PublicTests", pub_test)],
        "hidden_tests": [("HiddenTests", hid_test)],
        "public_tests_meta": [
            meta_entry(name, describe_core_case(spec["slug"], c))
            for c, (name, _) in zip(spec["public"], (cs_method(c) for c in spec["public"]))
        ],
    }


def build_rust(spec: dict) -> dict:
    fn = _fn_name(spec["slug"])
    rust_fn = fn
    if spec["slug"] == "gcd":
        rust_fn = "gcd"
        sig = "pub fn gcd(a: i32, b: i32) -> i32"
    elif spec["slug"] == "is-prime":
        rust_fn = "is_prime"
        sig = "pub fn is_prime(n: i32) -> bool"
    elif spec["slug"] == "contains-duplicate":
        rust_fn = "contains_duplicate"
        sig = "pub fn contains_duplicate(nums: &[i32]) -> bool"
    elif spec["slug"] == "missing-number":
        rust_fn = "missing_number"
        sig = "pub fn missing_number(nums: &[i32]) -> i32"
    elif spec["slug"] == "linear-search":
        rust_fn = "linear_search"
        sig = "pub fn linear_search(nums: &[i32], target: i32) -> i32"
    elif spec["slug"] == "sqrt-integer":
        rust_fn = "my_sqrt"
        sig = "pub fn my_sqrt(x: i32) -> i32"
    elif spec["slug"] == "power-of-two":
        rust_fn = "is_power_of_two"
        sig = "pub fn is_power_of_two(n: i32) -> bool"
    elif spec["slug"] == "climbing-stairs":
        rust_fn = "climb_stairs"
        sig = "pub fn climb_stairs(n: i32) -> i32"
    elif spec["slug"] == "fibonacci-number":
        rust_fn = "fib"
        sig = "pub fn fib(n: i32) -> i32"
    elif spec["slug"] == "factorial":
        rust_fn = "factorial"
        sig = "pub fn factorial(n: i32) -> i32"
    else:
        sig = f"pub fn {rust_fn}(n: i32) -> i32"

    starter = f"{sig} {{\n    todo!()\n}}\n"

    def rs_assert(case) -> str:
        if spec["slug"] in ("contains-duplicate", "missing-number"):
            nums = f"&{case[0]}"
            want = "true" if case[-1] else "false"
            return f"assert_eq!({rust_fn}({nums}), {want});"
        if spec["slug"] == "linear-search":
            nums = f"&{case[0]}"
            return f"assert_eq!({rust_fn}({nums}, {case[1]}), {case[2]});"
        if spec["slug"] in ("is-prime", "power-of-two"):
            want = "true" if case[1] else "false"
            return f"assert_eq!({rust_fn}({case[0]}), {want});"
        args = ", ".join(str(x) for x in case[:-1])
        return f"assert_eq!({rust_fn}({args}), {case[-1]});"

    def rust_test_file(cases: list, prefix: str) -> list[tuple[str, str]]:
        files: list[tuple[str, str]] = []
        for i, case in enumerate(cases):
            desc = describe_core_case(spec["slug"], case)
            fn_name = slugify_name(f"{prefix}_{desc}")
            src = f"""use challenge::{rust_fn};

#[test]
fn {fn_name}() {{
    {rs_assert(case)}
}}
"""
            files.append((fn_name, src))
        return files

    return {
        "slug": f"{spec['slug']}-rust",
        "title": f"{spec['title']} (Rust)",
        "difficulty": spec["difficulty"],
        "description": spec["description"],
        "language": "rust",
        "runtime": "1.84",
        "starter": starter,
        "public_tests": rust_test_file(spec["public"], "public"),
        "hidden_tests": rust_test_file(spec["hidden"], "hidden"),
        "public_tests_meta": [
            meta_entry(slugify_name(describe_core_case(spec["slug"], c)), describe_core_case(spec["slug"], c))
            for i, c in enumerate(spec["public"])
        ],
    }


def _cpp_fn(spec: dict) -> tuple[str, str, str]:
    """Return (function_name, return_type, params)."""
    if spec["slug"] == "gcd":
        return "gcd", "int", "int a, int b"
    if spec["slug"] == "is-prime":
        return "is_prime", "bool", "int n"
    if spec["slug"] == "contains-duplicate":
        return "contains_duplicate", "bool", "const std::vector<int>& nums"
    if spec["slug"] == "missing-number":
        return "missing_number", "int", "const std::vector<int>& nums"
    if spec["slug"] == "linear-search":
        return "linear_search", "int", "const std::vector<int>& nums, int target"
    if spec["slug"] == "sqrt-integer":
        return "my_sqrt", "int", "int x"
    if spec["slug"] == "power-of-two":
        return "is_power_of_two", "bool", "int n"
    if spec["slug"] == "climbing-stairs":
        return "climb_stairs", "int", "int n"
    if spec["slug"] == "fibonacci-number":
        return "fib", "int", "int n"
    return "factorial", "int", "int n"


def _cpp_vec(values: list) -> str:
    return "std::vector<int>{" + ", ".join(str(v) for v in values) + "}"


def build_cpp(spec: dict) -> dict:
    fn, ret, params = _cpp_fn(spec)
    starter = f"""#include <stdexcept>
#include <vector>

{ret} {fn}({params}) {{
    throw std::runtime_error("TODO");
}}
"""

    def cpp_require(case) -> str:
        if spec["slug"] == "linear-search":
            args = f"{_cpp_vec(case[0])}, {case[1]}"
            want = str(case[2])
        elif spec["slug"] in ("contains-duplicate", "missing-number"):
            args = _cpp_vec(case[0])
            want = "true" if case[1] is True else "false" if case[1] is False else str(case[1])
        elif spec["slug"] in ("is-prime", "power-of-two"):
            args = str(case[0])
            want = "true" if case[1] else "false"
        else:
            args = ", ".join(str(x) for x in case[:-1])
            want = str(case[-1]).lower() if isinstance(case[-1], bool) else str(case[-1])
        return f"REQUIRE({fn}({args}) == {want});"

    def cpp_test_file(cases: list, prefix: str) -> list[tuple[str, str]]:
        files: list[tuple[str, str]] = []
        for i, case in enumerate(cases):
            desc = describe_core_case(spec["slug"], case)
            case_name = slugify_name(f"{prefix}_{i + 1}")
            src = f"""#include <catch2/catch_test_macros.hpp>
#include <vector>

extern {ret} {fn}({params});

TEST_CASE("{desc}") {{
    {cpp_require(case)}
}}
"""
            files.append((case_name, src))
        return files

    return {
        "slug": f"{spec['slug']}-cpp",
        "title": f"{spec['title']} (C++)",
        "difficulty": spec["difficulty"],
        "description": spec["description"],
        "language": "cpp",
        "runtime": "20",
        "starter": starter,
        "public_tests": cpp_test_file(spec["public"], "public"),
        "hidden_tests": cpp_test_file(spec["hidden"], "hidden"),
        "public_tests_meta": [
            meta_entry(slugify_name(describe_core_case(spec["slug"], c)), describe_core_case(spec["slug"], c))
            for i, c in enumerate(spec["public"])
        ],
    }


GO_CHALLENGES = [build_go(s) for s in CORE_SPECS]
NODE_CHALLENGES = [build_node(s) for s in CORE_SPECS]
TYPESCRIPT_CHALLENGES = [build_typescript(s) for s in CORE_SPECS]
CSHARP_CHALLENGES = [build_csharp(s) for s in CORE_SPECS]
RUST_CHALLENGES = [build_rust(s) for s in CORE_SPECS]
CPP_CHALLENGES = [build_cpp(s) for s in CORE_SPECS]
