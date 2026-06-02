"""Build learner-facing challenge descriptions (task, context, examples, constraints)."""

from __future__ import annotations

import re

# Alternate slugs that share the same narrative as a canonical problem.
SLUG_ALIASES: dict[str, str] = {
    "best-time-buy-sell-stock": "best-time-stock",
    "reverse-string-java": "reverse-string",
    "valid-anagram-java": "valid-anagram",
}

# Extra narrative keyed by canonical slug (inspired by TheAlgorithms / classic DSA).
SLUG_CONTEXT: dict[str, str] = {
    "two-sum": (
        "You are given an array of integers and a target sum. "
        "Find **two different indices** whose values add up to the target. "
        "Exactly one valid pair exists."
    ),
    "valid-parentheses": (
        "A string is valid when every opening bracket has a matching closing bracket "
        "of the same type and pairs are properly nested. Ignore all other characters."
    ),
    "binary-search": (
        "The input array is sorted in ascending order. "
        "Return the index of `target`, or `-1` if it is not present."
    ),
    "reverse-string": (
        "Return a new string (or mutate in place where applicable) with characters in reverse order."
    ),
    "valid-palindrome": (
        "Consider only alphanumeric characters and ignore case when checking palindrome property."
    ),
    "max-subarray": (
        "Find the contiguous subarray with the largest sum and return that sum "
        "(Kadane's algorithm is the classic approach)."
    ),
    "single-number": (
        "Every element appears **twice** except for one element that appears once. Return that element."
    ),
    "plus-one": (
        "A non-negative integer is stored as an array of digits (most significant digit first). "
        "Return the digit array after adding one."
    ),
    "best-time-stock": (
        "Each array element is a stock price on a given day. "
        "You may complete **at most one** transaction: choose one day to buy and a later day to sell. "
        "Return the maximum profit, or `0` if no profit is possible."
    ),
    "merge-sorted-arrays": (
        "Both input arrays are sorted in non-decreasing order. "
        "Return a new sorted array containing all elements from both inputs."
    ),
    "bubble-sort": (
        "Return a **new** array sorted in ascending order using the bubble sort algorithm "
        "(do not mutate the input unless the API says otherwise)."
    ),
    "anagram-check": (
        "Two strings are anagrams if they contain the same characters with the same counts "
        "(comparison is case-sensitive unless stated otherwise)."
    ),
    "anagram-groups": (
        "Group strings that are anagrams of each other. Each input word appears in exactly one group. "
        "Order of groups and words within a group does not matter."
    ),
    "factorial": (
        "By definition, `0! = 1`. For `n > 0`, multiply all integers from `1` through `n`."
    ),
    "fibonacci": (
        "Use the standard definition: `F(0) = 0`, `F(1) = 1`, and `F(n) = F(n-1) + F(n-2)`."
    ),
    "is-prime": (
        "A prime number is an integer greater than `1` with no positive divisors other than `1` and itself."
    ),
    "gcd": (
        "The greatest common divisor (GCD) is the largest integer that divides both inputs without remainder."
    ),
    "contains-duplicate": (
        "Return `true` if any value appears at least twice in the array."
    ),
    "missing-number": (
        "The array `nums` contains `n` distinct values taken from the range `[0, n]`. "
        "Exactly one number from that range is missing."
    ),
    "climbing-stairs": (
        "You can climb 1 or 2 steps at a time. Count how many distinct ways you can reach step `n`."
    ),
    "linear-search": (
        "Return the index of `target` in the array, or `-1` if the value does not occur."
    ),
    "sqrt-integer": (
        "Return the integer part of the square root of `x` (truncate toward zero)."
    ),
    "power-of-two": (
        "Return whether `n` is a power of two (including `1 = 2^0`)."
    ),
    "valid-anagram": (
        "Given two strings `s` and `t`, return whether `t` is an anagram of `s`."
    ),
    "best-time-buy-sell-stock": (
        "Each element of `prices` is the stock price on day `i`. "
        "Pick at most one buy day and one sell day after it to maximize profit."
    ),
}


def normalize_slug(slug: str) -> str:
    slug = SLUG_ALIASES.get(slug, slug)
    for suffix in (
        "-go",
        "-node",
        "-typescript",
        "-csharp",
        "-rust",
        "-cpp",
        "-python",
        "-java",
        "-react",
        "-vue",
        "-angular",
    ):
        if slug.endswith(suffix):
            return slug[: -len(suffix)]
    return slug


# Curated examples when public_tests_meta cannot express the return type (e.g. List<List<String>>).
SLUG_EXAMPLE_ROWS: dict[str, list[tuple[str, str]]] = {
    "anagram-groups": [
        ('["eat", "tea", "tan", "ate", "nat", "bat"]', '[["bat"], ["nat", "tan"], ["eat", "tea", "ate"]]'),
        ("[]", "[]"),
        ('["a"]', '[["a"]]'),
    ],
}


def format_examples_markdown(meta: list[dict[str, str]], slug: str = "") -> str:
    from example_rows import format_examples_markdown as _format_rows
    from example_rows import rows_from_meta

    return _format_rows(rows_from_meta(meta, slug))


def _already_rich(description: str) -> bool:
    return bool(re.search(r"^##\s+what to do\b", description, re.I | re.M)) and len(description) > 180


def _extract_signature(description: str) -> str | None:
    solution = re.search(r"`(Solution\.[^`]+)`", description)
    if solution:
        return solution.group(1).strip()
    match = re.search(r"`([^`]+)`", description)
    return match.group(1).strip() if match else None


def _infer_background(slug: str, title: str, description: str, language: str) -> str:
    text = f"{slug} {title} {description}".lower()
    if language in ("react", "vue", "angular"):
        return (
            "Implement the component or module described above so it passes the Vitest tests. "
            "Focus on rendering, user interaction, and predictable state — not routing or backend APIs."
        )
    if "stock" in slug or "profit" in text or "prices" in text:
        return SLUG_CONTEXT["best-time-stock"]
    if "anagram-groups" in slug:
        return SLUG_CONTEXT["anagram-groups"]
    if "anagram" in slug:
        return SLUG_CONTEXT["anagram-check"]
    if "palindrome" in slug:
        return SLUG_CONTEXT["valid-palindrome"]
    if "parentheses" in slug or "bracket" in text:
        return SLUG_CONTEXT["valid-parentheses"]
    if "two-sum" in slug:
        return SLUG_CONTEXT["two-sum"]
    if "binary-search" in slug:
        return SLUG_CONTEXT["binary-search"]
    if "subarray" in slug:
        return SLUG_CONTEXT["max-subarray"]
    if "factorial" in slug:
        return SLUG_CONTEXT["factorial"]
    if "fibonacci" in slug:
        return SLUG_CONTEXT["fibonacci"]
    if "prime" in slug:
        return SLUG_CONTEXT["is-prime"]
    if "gcd" in slug:
        return SLUG_CONTEXT["gcd"]
    if "duplicate" in slug:
        return SLUG_CONTEXT["contains-duplicate"]
    if "missing-number" in slug or "missing number" in title.lower():
        return SLUG_CONTEXT["missing-number"]
    if "stair" in slug:
        return SLUG_CONTEXT["climbing-stairs"]
    if "merge" in slug and "sort" in slug:
        return SLUG_CONTEXT["merge-sorted-arrays"]
    if "bubble" in slug:
        return SLUG_CONTEXT["bubble-sort"]
    if "reverse" in slug and "string" in slug:
        return SLUG_CONTEXT["reverse-string"]
    if "search" in slug:
        return "Search the structure or array as described and return the required index or boolean result."
    if "sort" in slug:
        return "Produce sorted output according to the rules in the task line; preserve stability if required."
    if "cipher" in slug or "encrypt" in slug:
        return "Apply the transformation character by character; preserve case and non-alphabetic symbols unless stated."
    if "armstrong" in slug:
        return (
            "An Armstrong number equals the sum of its digits each raised to the power of the number of digits "
            "(e.g. 153 = 1³ + 5³ + 3³)."
        )
    return (
        "Read the task and signature carefully. "
        "Use the public examples to clarify edge cases before submitting."
    )


def _constraints(language: str, difficulty: str, description: str) -> str:
    lines = [
        "Implement only the required API; do not change test files.",
        "Hidden tests may include additional edge cases beyond the examples.",
    ]
    if language == "java":
        lines.append("Use the `Solution` class and static methods unless the starter specifies otherwise.")
    elif language == "python":
        lines.append("Put your solution in `starter/solution.py` with the expected function names.")
    elif language in ("react", "vue", "angular"):
        lines.append("Export the component/service/pipe expected by the Vitest suite.")
    elif language == "go":
        lines.append("Use package `solution` and the function signature from the starter file.")
    if difficulty == "medium":
        lines.append("Aim for an efficient solution; brute force may time out on large hidden inputs.")
    sig = _extract_signature(description)
    if sig and "int[]" in sig:
        lines.append("Arrays may be empty unless examples show otherwise.")
    return "\n".join(f"- {line}" for line in lines)


def build_description_md(
    slug: str,
    title: str,
    difficulty: str,
    language: str,
    base_description: str,
    public_meta: list[dict[str, str]],
) -> str:
    """Structured markdown: What to do → context → Examples → Constraints → signature."""
    base = base_description.strip()
    if _already_rich(base):
        examples = format_examples_markdown(public_meta, slug)
        has_examples = bool(
            re.search(r"^##\s+examples?\b", base, re.I | re.M)
            or re.search(r"\*\*examples?\*\*", base, re.I)
        )
        if examples and not has_examples:
            from example_rows import rows_from_meta, upsert_examples_section

            rows = rows_from_meta(public_meta, slug)
            if rows:
                return upsert_examples_section(base, rows)
            return base + examples
        return base

    canonical = normalize_slug(slug)
    parts: list[str] = ["## What to do\n", base]

    context = SLUG_CONTEXT.get(canonical) or SLUG_CONTEXT.get(slug)
    if not context:
        context = _infer_background(canonical, title, base, language)
    if context and context not in base:
        parts.append(f"\n\n{context}")

    if not re.search(r"^##\s+examples?\b", base, re.I | re.M) and not re.search(
        r"\*\*examples?\*\*", base, re.I
    ):
        examples = format_examples_markdown(public_meta, slug)
        if examples:
            parts.append(examples)

    parts.append("\n\n## Constraints\n")
    parts.append(_constraints(language, difficulty, base))

    signature = _extract_signature(base)
    if signature:
        parts.append(f"\n\n## Method to implement\n\n`{signature}`")

    return "".join(parts)
