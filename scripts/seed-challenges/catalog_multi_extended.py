"""
Extended algorithm catalog for Go, Node, TypeScript, C#, Rust, C++.
Inspired by TheAlgorithms, rustlings-style basics, and classic DSA.
"""

from __future__ import annotations

import json

from test_descriptions import describe_extended_case, escape_cpp_string_literal, meta_entry, slugify_name

EXTENDED_CORE_SPECS = [
    {
        "slug": "two-sum",
        "title": "Two Sum",
        "difficulty": "easy",
        "description": "Return 0-based indices of two distinct elements that sum to `target` (exactly one solution).",
        "public": [([2, 7, 11, 15], 9, [0, 1]), ([3, 2, 4], 6, [1, 2])],
        "hidden": [([3, 3], 6, [0, 1]), ([-1, -2, -3], -5, [1, 2])],
    },
    {
        "slug": "valid-parentheses",
        "title": "Valid Parentheses",
        "difficulty": "easy",
        "description": "Return whether `()`, `[]`, and `{}` brackets are balanced and properly nested.",
        "public": [("()", True), ("()[]{}", True)],
        "hidden": [("(]", False), ("{[]}", True)],
    },
    {
        "slug": "binary-search",
        "title": "Binary Search",
        "difficulty": "easy",
        "description": "Return index of `target` in sorted `nums`, or -1 if absent.",
        "public": [([1, 3, 5, 7, 9], 3, 1), ([1, 3, 5, 7, 9], 4, -1)],
        "hidden": [([2, 4, 6], 2, 0), ([], 1, -1)],
    },
    {
        "slug": "reverse-string",
        "title": "Reverse String",
        "difficulty": "easy",
        "description": "Return the reverse of the input string.",
        "public": [("hello", "olleh"), ("", "")],
        "hidden": [("a", "a"), ("race", "ecar")],
    },
    {
        "slug": "valid-palindrome",
        "title": "Valid Palindrome",
        "difficulty": "easy",
        "description": "After removing non-alphanumeric characters and lowercasing, return whether the string is a palindrome.",
        "public": [("A man, a plan, a canal: Panama", True), ("race a car", False)],
        "hidden": [("", True), (" ", True)],
    },
    {
        "slug": "max-subarray",
        "title": "Maximum Subarray",
        "difficulty": "medium",
        "description": "Return the largest sum of any contiguous subarray (Kadane).",
        "public": [([-2, 1, -3, 4, -1, 2, 1, -4, 3], 6), ([1], 1)],
        "hidden": [([5, 4, -1, 7, 8], 23), ([-1], -1)],
    },
    {
        "slug": "single-number",
        "title": "Single Number",
        "difficulty": "easy",
        "description": "Every element appears twice except one; return that element.",
        "public": [([2, 2, 1], 1), ([4, 1, 2, 1, 2], 4)],
        "hidden": [([1], 1), ([6, 3, 6], 3)],
    },
    {
        "slug": "plus-one",
        "title": "Plus One",
        "difficulty": "easy",
        "description": "Increment a non-negative integer stored as digit array (MSD first); return new digits.",
        "public": [([1, 2, 3], [1, 2, 4]), ([9], [1, 0])],
        "hidden": [([0], [1]), ([9, 9, 9], [1, 0, 0, 0])],
    },
    {
        "slug": "best-time-stock",
        "title": "Best Time to Buy and Sell Stock",
        "difficulty": "easy",
        "description": "Return maximum profit from one buy and one sell; 0 if no profit possible.",
        "public": [([7, 1, 5, 3, 6, 4], 5), ([7, 6, 4, 3, 1], 0)],
        "hidden": [([2, 4, 1], 2), ([1, 2], 1)],
    },
    {
        "slug": "merge-sorted-arrays",
        "title": "Merge Sorted Arrays",
        "difficulty": "easy",
        "description": "Merge two sorted integer arrays into one sorted array.",
        "public": [([1, 2, 3], [2, 5, 6], [1, 2, 2, 3, 5, 6]), ([], [1], [1])],
        "hidden": [([1], [], [1]), ([1, 1], [1], [1, 1, 1])],
    },
    {
        "slug": "bubble-sort",
        "title": "Bubble Sort",
        "difficulty": "easy",
        "description": "Return a new array sorted ascending using bubble sort (stable).",
        "public": [([3, 1, 2], [1, 2, 3]), ([1], [1])],
        "hidden": [([], []), ([5, 4, 3, 2, 1], [1, 2, 3, 4, 5])],
    },
    {
        "slug": "anagram-check",
        "title": "Anagram Check",
        "difficulty": "easy",
        "description": "Return true if `s` and `t` are anagrams (case-sensitive).",
        "public": [("anagram", "nagaram", True), ("rat", "car", False)],
        "hidden": [("a", "a", True), ("ab", "a", False)],
    },
]

EXTENDED_SLUGS = {s["slug"] for s in EXTENDED_CORE_SPECS}


def _arr_py(a: list) -> str:
    return json.dumps(a)


def _arr_go(a: list) -> str:
    return "[]int{" + ", ".join(str(x) for x in a) + "}"


def _arr_cs(a: list) -> str:
    return "new int[] { " + ", ".join(str(x) for x in a) + " }"


def _arr_rust(a: list) -> str:
    return f"&{a}"


def _arr_cpp(a: list) -> str:
    return "std::vector<int>{" + ", ".join(str(x) for x in a) + "}"


def _slug_api(slug: str) -> dict:
    """Per-language export names and signatures."""
    m = {
        "two-sum": {
            "go": ("TwoSum", "func TwoSum(nums []int, target int) []int"),
            "js": "twoSum",
            "cs": "TwoSum",
            "rust": ("two_sum", "pub fn two_sum(nums: &[i32], target: i32) -> Vec<i32>"),
            "cpp": ("two_sum", "std::vector<int>", "const std::vector<int>& nums, int target"),
            "ts_params": "nums: number[], target: number",
            "ts_ret": "number[]",
        },
        "valid-parentheses": {
            "go": ("IsValidParentheses", "func IsValidParentheses(s string) bool"),
            "js": "isValidParentheses",
            "cs": "IsValidParentheses",
            "rust": ("is_valid_parentheses", "pub fn is_valid_parentheses(s: &str) -> bool"),
            "cpp": ("is_valid_parentheses", "bool", "const std::string& s"),
            "ts_params": "s: string",
            "ts_ret": "boolean",
        },
        "binary-search": {
            "go": ("BinarySearch", "func BinarySearch(nums []int, target int) int"),
            "js": "binarySearch",
            "cs": "BinarySearch",
            "rust": ("binary_search", "pub fn binary_search(nums: &[i32], target: i32) -> i32"),
            "cpp": ("binary_search", "int", "const std::vector<int>& nums, int target"),
            "ts_params": "nums: number[], target: number",
            "ts_ret": "number",
        },
        "reverse-string": {
            "go": ("ReverseString", "func ReverseString(s string) string"),
            "js": "reverseString",
            "cs": "ReverseString",
            "rust": ("reverse_string", "pub fn reverse_string(s: &str) -> String"),
            "cpp": ("reverse_string", "std::string", "const std::string& s"),
            "ts_params": "s: string",
            "ts_ret": "string",
        },
        "valid-palindrome": {
            "go": ("IsPalindrome", "func IsPalindrome(s string) bool"),
            "js": "isPalindrome",
            "cs": "IsPalindrome",
            "rust": ("is_palindrome", "pub fn is_palindrome(s: &str) -> bool"),
            "cpp": ("is_palindrome", "bool", "const std::string& s"),
            "ts_params": "s: string",
            "ts_ret": "boolean",
        },
        "max-subarray": {
            "go": ("MaxSubArray", "func MaxSubArray(nums []int) int"),
            "js": "maxSubArray",
            "cs": "MaxSubArray",
            "rust": ("max_sub_array", "pub fn max_sub_array(nums: &[i32]) -> i32"),
            "cpp": ("max_sub_array", "int", "const std::vector<int>& nums"),
            "ts_params": "nums: number[]",
            "ts_ret": "number",
        },
        "single-number": {
            "go": ("SingleNumber", "func SingleNumber(nums []int) int"),
            "js": "singleNumber",
            "cs": "SingleNumber",
            "rust": ("single_number", "pub fn single_number(nums: &[i32]) -> i32"),
            "cpp": ("single_number", "int", "const std::vector<int>& nums"),
            "ts_params": "nums: number[]",
            "ts_ret": "number",
        },
        "plus-one": {
            "go": ("PlusOne", "func PlusOne(digits []int) []int"),
            "js": "plusOne",
            "cs": "PlusOne",
            "rust": ("plus_one", "pub fn plus_one(digits: &[i32]) -> Vec<i32>"),
            "cpp": ("plus_one", "std::vector<int>", "const std::vector<int>& digits"),
            "ts_params": "digits: number[]",
            "ts_ret": "number[]",
        },
        "best-time-stock": {
            "go": ("MaxProfit", "func MaxProfit(prices []int) int"),
            "js": "maxProfit",
            "cs": "MaxProfit",
            "rust": ("max_profit", "pub fn max_profit(prices: &[i32]) -> i32"),
            "cpp": ("max_profit", "int", "const std::vector<int>& prices"),
            "ts_params": "prices: number[]",
            "ts_ret": "number",
        },
        "merge-sorted-arrays": {
            "go": ("MergeSorted", "func MergeSorted(a, b []int) []int"),
            "js": "mergeSorted",
            "cs": "MergeSorted",
            "rust": ("merge_sorted", "pub fn merge_sorted(a: &[i32], b: &[i32]) -> Vec<i32>"),
            "cpp": ("merge_sorted", "std::vector<int>", "const std::vector<int>& a, const std::vector<int>& b"),
            "ts_params": "a: number[], b: number[]",
            "ts_ret": "number[]",
        },
        "bubble-sort": {
            "go": ("BubbleSort", "func BubbleSort(nums []int) []int"),
            "js": "bubbleSort",
            "cs": "BubbleSort",
            "rust": ("bubble_sort", "pub fn bubble_sort(nums: &[i32]) -> Vec<i32>"),
            "cpp": ("bubble_sort", "std::vector<int>", "const std::vector<int>& nums"),
            "ts_params": "nums: number[]",
            "ts_ret": "number[]",
        },
        "anagram-check": {
            "go": ("IsAnagram", "func IsAnagram(s, t string) bool"),
            "js": "isAnagram",
            "cs": "IsAnagram",
            "rust": ("is_anagram", "pub fn is_anagram(s: &str, t: &str) -> bool"),
            "cpp": ("is_anagram", "bool", "const std::string& s, const std::string& t"),
            "ts_params": "s: string, t: string",
            "ts_ret": "boolean",
        },
    }
    return m[slug]


def _go_call(slug: str, case: tuple) -> tuple[str, str]:
    api = _slug_api(slug)
    fn = api["go"][0]
    if slug == "two-sum":
        return f"solution.{fn}({_arr_go(case[0])}, {case[1]})", _arr_go(case[2])
    if slug == "valid-parentheses":
        return f'solution.{fn}("{case[0]}")', "true" if case[1] else "false"
    if slug == "binary-search":
        return f"solution.{fn}({_arr_go(case[0])}, {case[1]})", str(case[2])
    if slug == "reverse-string":
        return f'solution.{fn}("{case[0]}")', f'"{case[1]}"'
    if slug == "valid-palindrome":
        return f'solution.{fn}("{case[0]}")', "true" if case[1] else "false"
    if slug == "max-subarray":
        return f"solution.{fn}({_arr_go(case[0])})", str(case[1])
    if slug == "single-number":
        return f"solution.{fn}({_arr_go(case[0])})", str(case[1])
    if slug == "plus-one":
        got = f"solution.{fn}({_arr_go(case[0])})"
        return got, _arr_go(case[1])
    if slug == "best-time-stock":
        return f"solution.{fn}({_arr_go(case[0])})", str(case[1])
    if slug == "merge-sorted-arrays":
        return f"solution.{fn}({_arr_go(case[0])}, {_arr_go(case[1])})", _arr_go(case[2])
    if slug == "bubble-sort":
        return f"solution.{fn}({_arr_go(case[0])})", _arr_go(case[1])
    if slug == "anagram-check":
        return f'solution.{fn}("{case[0]}", "{case[1]}")', "true" if case[2] else "false"
    raise ValueError(slug)


def build_go_extended(spec: dict) -> dict:
    slug = spec["slug"]
    api = _slug_api(slug)
    _, sig = api["go"]
    starter = f"package solution\n\n{sig} {{\n\tpanic(\"TODO\")\n}}\n"

    def go_test_file(cases: list, prefix: str) -> list[tuple[str, str]]:
        files: list[tuple[str, str]] = []
        for case in cases:
            desc = describe_extended_case(slug, case)
            test_id = slugify_name(f"{prefix}_{desc}")
            fn_name = "".join(p.capitalize() for p in test_id.split("_"))
            call, want = _go_call(slug, case)
            if slug in ("two-sum", "plus-one", "merge-sorted-arrays", "bubble-sort"):
                body = f"got := {call}\nwant := {want}\nif len(got) != len(want) {{ t.Fatal(\"length\") }}\nfor i := range want {{ if got[i] != want[i] {{ t.Fatal(\"unexpected\") }} }}"
            elif want in ("true", "false"):
                body = f"if {call} != {want} {{ t.Fatal(\"unexpected\") }}"
            elif want.startswith('"'):
                body = f'if {call} != {want} {{ t.Fatal("unexpected") }}'
            else:
                body = f"if {call} != {want} {{ t.Fatal(\"unexpected\") }}"
            src = f"""package solution_test

import (
	"testing"

	"challenge/solution"
)

func Test{fn_name}(t *testing.T) {{
	{body}
}}
"""
            files.append((test_id + "_test", src))
        return files

    return {
        "slug": f"{slug}-go",
        "title": f"{spec['title']} (Go)",
        "difficulty": spec["difficulty"],
        "description": spec["description"],
        "language": "go",
        "runtime": "1.23",
        "starter": starter,
        "public_tests": go_test_file(spec["public"], "public"),
        "hidden_tests": go_test_file(spec["hidden"], "hidden"),
        "public_tests_meta": [
            meta_entry(slugify_name(describe_extended_case(slug, c)), describe_extended_case(slug, c))
            for c in spec["public"]
        ],
    }


def _js_call(slug: str, case: tuple, fn: str) -> str:
    if slug == "two-sum":
        return f"assert.deepEqual({fn}({_arr_py(case[0])}, {case[1]}), {json.dumps(case[2])});"
    if slug == "valid-parentheses":
        return f"assert.equal({fn}({json.dumps(case[0])}), {str(case[1]).lower()});"
    if slug == "binary-search":
        return f"assert.equal({fn}({_arr_py(case[0])}, {case[1]}), {case[2]});"
    if slug == "reverse-string":
        return f"assert.equal({fn}({json.dumps(case[0])}), {json.dumps(case[1])});"
    if slug == "valid-palindrome":
        return f"assert.equal({fn}({json.dumps(case[0])}), {str(case[1]).lower()});"
    if slug == "max-subarray":
        return f"assert.equal({fn}({_arr_py(case[0])}), {case[1]});"
    if slug == "single-number":
        return f"assert.equal({fn}({_arr_py(case[0])}), {case[1]});"
    if slug == "plus-one":
        return f"assert.deepEqual({fn}({_arr_py(case[0])}), {json.dumps(case[1])});"
    if slug == "best-time-stock":
        return f"assert.equal({fn}({_arr_py(case[0])}), {case[1]});"
    if slug == "merge-sorted-arrays":
        return f"assert.deepEqual({fn}({_arr_py(case[0])}, {_arr_py(case[1])}), {json.dumps(case[2])});"
    if slug == "bubble-sort":
        return f"assert.deepEqual({fn}({_arr_py(case[0])}), {json.dumps(case[1])});"
    if slug == "anagram-check":
        return f"assert.equal({fn}({json.dumps(case[0])}, {json.dumps(case[1])}), {str(case[2]).lower()});"
    raise ValueError(slug)


def build_node_extended(spec: dict) -> dict:
    slug = spec["slug"]
    fn = _slug_api(slug)["js"]
    starter = f"""function {fn}(/* args */) {{
  throw new Error("TODO");
}}

module.exports = {{ {fn} }};
"""

    def node_test_file(cases: list, prefix: str) -> list[tuple[str, str]]:
        files: list[tuple[str, str]] = []
        for i, case in enumerate(cases):
            desc = describe_extended_case(slug, case)
            test_id = slugify_name(f"{prefix}_{i + 1}")
            src = f"""const {{ test }} = require("node:test");
const assert = require("node:assert/strict");
const {{ {fn} }} = require("../solution.js");

test("{desc}", () => {{
  {_js_call(slug, case, fn)}
}});
"""
            files.append((test_id, src))
        return files

    return {
        "slug": f"{slug}-node",
        "title": f"{spec['title']} (Node.js)",
        "difficulty": spec["difficulty"],
        "description": spec["description"],
        "language": "node",
        "runtime": "22",
        "starter": starter,
        "public_tests": node_test_file(spec["public"], "public"),
        "hidden_tests": node_test_file(spec["hidden"], "hidden"),
        "public_tests_meta": [
            meta_entry(slugify_name(describe_extended_case(slug, c)), describe_extended_case(slug, c))
            for c in spec["public"]
        ],
    }


def build_typescript_extended(spec: dict) -> dict:
    slug = spec["slug"]
    api = _slug_api(slug)
    fn = api["js"]
    entry = {
        "slug": f"{slug}-typescript",
        "title": f"{spec['title']} (TypeScript)",
        "difficulty": spec["difficulty"],
        "description": spec["description"],
        "language": "typescript",
        "runtime": "5.7",
        "starter": f"export function {fn}({api['ts_params']}): {api['ts_ret']} {{\n  throw new Error('TODO');\n}}\n",
    }

    def ts_test_file(cases: list, prefix: str) -> list[tuple[str, str]]:
        files: list[tuple[str, str]] = []
        for i, case in enumerate(cases):
            desc = describe_extended_case(slug, case)
            test_id = slugify_name(f"{prefix}_{i + 1}")
            src = f"""import {{ test }} from "node:test";
import assert from "node:assert/strict";
import {{ {fn} }} from "../solution.ts";

test("{desc}", () => {{
  {_js_call(slug, case, fn)}
}});
"""
            files.append((test_id, src))
        return files

    entry["public_tests"] = ts_test_file(spec["public"], "public")
    entry["hidden_tests"] = ts_test_file(spec["hidden"], "hidden")
    entry["public_tests_meta"] = [
        meta_entry(slugify_name(describe_extended_case(slug, c)), describe_extended_case(slug, c))
        for c in spec["public"]
    ]
    return entry


def _cs_assert(slug: str, case: tuple, fn: str) -> str:
    if slug == "two-sum":
        exp = _arr_cs(case[2])
        return f"Assert.Equal({exp}, Solution.{fn}({_arr_cs(case[0])}, {case[1]}));"
    if slug in ("plus-one", "merge-sorted-arrays", "bubble-sort"):
        exp = _arr_cs(case[-1] if slug != "two-sum" else case[2])
        if slug == "merge-sorted-arrays":
            args = f"{_arr_cs(case[0])}, {_arr_cs(case[1])}"
        else:
            args = _arr_cs(case[0])
        return f"Assert.Equal({exp}, Solution.{fn}({args}));"
    if slug in ("valid-parentheses", "valid-palindrome", "anagram-check"):
        if slug == "anagram-check":
            args = f'"{case[0]}", "{case[1]}"'
            want = str(case[2]).lower()
        else:
            args = f'"{case[0]}"'
            want = str(case[1]).lower()
        return f"Assert.Equal({want}, Solution.{fn}({args}));"
    if slug == "reverse-string":
        return f'Assert.Equal("{case[1]}", Solution.{fn}("{case[0]}"));'
    if slug == "binary-search":
        return f"Assert.Equal({case[2]}, Solution.{fn}({_arr_cs(case[0])}, {case[1]}));"
    if slug in ("max-subarray", "single-number", "best-time-stock"):
        args = _arr_cs(case[0])
        return f"Assert.Equal({case[1]}, Solution.{fn}({args}));"
    raise ValueError(slug)


def build_csharp_extended(spec: dict) -> dict:
    slug = spec["slug"]
    fn = _slug_api(slug)["cs"]
    api = _slug_api(slug)
    ret = api["ts_ret"].replace("number[]", "int[]").replace("boolean", "bool").replace("number", "int").replace("string", "string")
    params = api["ts_params"].replace("number[]", "int[]").replace("number", "int")
    starter = f"""namespace Challenge;

public static class Solution
{{
    public static {ret} {fn}({params})
    {{
        throw new NotImplementedException();
    }}
}}
"""

    def cs_method(case: tuple) -> tuple[str, str]:
        desc = describe_extended_case(slug, case)
        method = "".join(p.capitalize() for p in slugify_name(desc).split("_"))
        return method, _cs_assert(slug, case, fn)

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
        "slug": f"{slug}-csharp",
        "title": f"{spec['title']} (C#)",
        "difficulty": spec["difficulty"],
        "description": spec["description"],
        "language": "csharp",
        "runtime": "8.0",
        "starter": starter,
        "public_tests": [("PublicTests", pub_test)],
        "hidden_tests": [("HiddenTests", hid_test)],
        "public_tests_meta": [
            meta_entry(name, describe_extended_case(slug, c))
            for c, (name, _) in zip(spec["public"], (cs_method(c) for c in spec["public"]))
        ],
    }


def _rust_assert(slug: str, case: tuple, fn: str) -> str:
    def vec(nums: list) -> str:
        return "vec![" + ", ".join(str(x) for x in nums) + "]"

    if slug == "two-sum":
        return f"assert_eq!({fn}({_arr_rust(case[0])}, {case[1]}), {vec(case[2])});"
    if slug in ("plus-one", "merge-sorted-arrays", "bubble-sort"):
        if slug == "merge-sorted-arrays":
            return f"assert_eq!({fn}({_arr_rust(case[0])}, {_arr_rust(case[1])}), {vec(case[2])});"
        return f"assert_eq!({fn}({_arr_rust(case[0])}), {vec(case[1])});"
    if slug in ("valid-parentheses", "valid-palindrome", "anagram-check"):
        if slug == "anagram-check":
            return f'assert_eq!({fn}("{case[0]}", "{case[1]}"), {str(case[2]).lower()});'
        return f'assert_eq!({fn}("{case[0]}"), {str(case[1]).lower()});'
    if slug == "reverse-string":
        return f'assert_eq!({fn}("{case[0]}"), "{case[1]}");'
    if slug == "binary-search":
        return f"assert_eq!({fn}({_arr_rust(case[0])}, {case[1]}), {case[2]});"
    return f"assert_eq!({fn}({_arr_rust(case[0])}), {case[1]});"


def build_rust_extended(spec: dict) -> dict:
    slug = spec["slug"]
    fn, sig = _slug_api(slug)["rust"]
    starter = f"{sig} {{\n    todo!()\n}}\n"

    def rust_test_file(cases: list, prefix: str) -> list[tuple[str, str]]:
        files: list[tuple[str, str]] = []
        for case in cases:
            desc = describe_extended_case(slug, case)
            fn_name = slugify_name(f"{prefix}_{desc}")
            src = f"""use challenge::{fn};

#[test]
fn {fn_name}() {{
    {_rust_assert(slug, case, fn)}
}}
"""
            files.append((fn_name, src))
        return files

    return {
        "slug": f"{slug}-rust",
        "title": f"{spec['title']} (Rust)",
        "difficulty": spec["difficulty"],
        "description": spec["description"],
        "language": "rust",
        "runtime": "1.84",
        "starter": starter,
        "public_tests": rust_test_file(spec["public"], "public"),
        "hidden_tests": rust_test_file(spec["hidden"], "hidden"),
        "public_tests_meta": [
            meta_entry(slugify_name(describe_extended_case(slug, c)), describe_extended_case(slug, c))
            for c in spec["public"]
        ],
    }


def _cpp_assert(slug: str, case: tuple, fn: str, ret: str, params: str) -> str:
    if slug == "two-sum":
        exp = _arr_cpp(case[2])
        return f"REQUIRE({fn}({_arr_cpp(case[0])}, {case[1]}) == {exp});"
    if slug in ("plus-one", "merge-sorted-arrays", "bubble-sort"):
        if slug == "merge-sorted-arrays":
            return f"REQUIRE({fn}({_arr_cpp(case[0])}, {_arr_cpp(case[1])}) == {_arr_cpp(case[2])});"
        return f"REQUIRE({fn}({_arr_cpp(case[0])}) == {_arr_cpp(case[1])});"
    if slug in ("valid-parentheses", "valid-palindrome", "anagram-check"):
        if slug == "anagram-check":
            return f'REQUIRE({fn}("{case[0]}", "{case[1]}") == {"true" if case[2] else "false"});'
        return f'REQUIRE({fn}("{case[0]}") == {"true" if case[1] else "false"});'
    if slug == "reverse-string":
        return f'REQUIRE({fn}("{case[0]}") == "{case[1]}");'
    if slug == "binary-search":
        return f"REQUIRE({fn}({_arr_cpp(case[0])}, {case[1]}) == {case[2]});"
    return f"REQUIRE({fn}({_arr_cpp(case[0])}) == {case[1]});"


def build_cpp_extended(spec: dict) -> dict:
    slug = spec["slug"]
    fn, ret, params = _slug_api(slug)["cpp"]
    starter = f"""#include <stdexcept>
#include <string>
#include <vector>

{ret} {fn}({params}) {{
    throw std::runtime_error("TODO");
}}
"""

    def cpp_test_file(cases: list, prefix: str) -> list[tuple[str, str]]:
        files: list[tuple[str, str]] = []
        for i, case in enumerate(cases):
            desc = describe_extended_case(slug, case)
            case_name = slugify_name(f"{prefix}_{i + 1}")
            src = f"""#include <catch2/catch_test_macros.hpp>
#include <string>
#include <vector>

extern {ret} {fn}({params});

TEST_CASE("{escape_cpp_string_literal(desc)}") {{
    {_cpp_assert(slug, case, fn, ret, params)}
}}
"""
            files.append((case_name, src))
        return files

    return {
        "slug": f"{slug}-cpp",
        "title": f"{spec['title']} (C++)",
        "difficulty": spec["difficulty"],
        "description": spec["description"],
        "language": "cpp",
        "runtime": "20",
        "starter": starter,
        "public_tests": cpp_test_file(spec["public"], "public"),
        "hidden_tests": cpp_test_file(spec["hidden"], "hidden"),
        "public_tests_meta": [
            meta_entry(slugify_name(describe_extended_case(slug, c)), describe_extended_case(slug, c))
            for c in spec["public"]
        ],
    }


EXTENDED_GO_CHALLENGES = [build_go_extended(s) for s in EXTENDED_CORE_SPECS]
EXTENDED_NODE_CHALLENGES = [build_node_extended(s) for s in EXTENDED_CORE_SPECS]
EXTENDED_TYPESCRIPT_CHALLENGES = [build_typescript_extended(s) for s in EXTENDED_CORE_SPECS]
EXTENDED_CSHARP_CHALLENGES = [build_csharp_extended(s) for s in EXTENDED_CORE_SPECS]
EXTENDED_RUST_CHALLENGES = [build_rust_extended(s) for s in EXTENDED_CORE_SPECS]
EXTENDED_CPP_CHALLENGES = [build_cpp_extended(s) for s in EXTENDED_CORE_SPECS]
