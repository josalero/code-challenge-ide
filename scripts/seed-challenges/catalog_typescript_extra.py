"""TypeScript utility challenges — TheAlgorithms / handbook-style (runtime, not type-only)."""

TYPESCRIPT_EXTRA_CHALLENGES = [
    {
        "slug": "capitalize-words-typescript",
        "title": "Capitalize Words (TypeScript)",
        "difficulty": "easy",
        "description": "Return `text` with the first character of each word uppercased (words separated by spaces).",
        "language": "typescript",
        "runtime": "5.7",
        "starter": "export function capitalizeWords(text: string): string {\n  throw new Error('TODO');\n}\n",
        "public_tests": [
            (
                "capitalize_public",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { capitalizeWords } from "../solution.ts";

test("capitalizes each word", () => {
  assert.equal(capitalizeWords("hello world"), "Hello World");
});

test("single word", () => {
  assert.equal(capitalizeWords("ada"), "Ada");
});
""",
            )
        ],
        "hidden_tests": [
            (
                "capitalize_hidden",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { capitalizeWords } from "../solution.ts";

test("multiple spaces preserved loosely", () => {
  assert.equal(capitalizeWords("a b"), "A B");
});
""",
            )
        ],
    },
    {
        "slug": "chunk-array-typescript",
        "title": "Chunk Array (TypeScript)",
        "difficulty": "easy",
        "description": "Split `items` into sub-arrays of length `size` (last chunk may be shorter).",
        "language": "typescript",
        "runtime": "5.7",
        "starter": "export function chunk<T>(items: T[], size: number): T[][] {\n  throw new Error('TODO');\n}\n",
        "public_tests": [
            (
                "chunk_public",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { chunk } from "../solution.ts";

test("chunks by two", () => {
  assert.deepEqual(chunk([1, 2, 3, 4, 5], 2), [[1, 2], [3, 4], [5]]);
});

test("size larger than array", () => {
  assert.deepEqual(chunk([1], 3), [[1]]);
});
""",
            )
        ],
        "hidden_tests": [
            (
                "chunk_hidden",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { chunk } from "../solution.ts";

test("empty input", () => {
  assert.deepEqual(chunk([], 2), []);
});
""",
            )
        ],
    },
    {
        "slug": "flatten-array-typescript",
        "title": "Flatten Array (TypeScript)",
        "difficulty": "easy",
        "description": "Flatten one level of nested arrays.",
        "language": "typescript",
        "runtime": "5.7",
        "starter": "export function flatten<T>(nested: (T | T[])[]): T[] {\n  throw new Error('TODO');\n}\n",
        "public_tests": [
            (
                "flatten_public",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { flatten } from "../solution.ts";

test("flattens one level", () => {
  assert.deepEqual(flatten([1, [2, 3], 4]), [1, 2, 3, 4]);
});

test("already flat", () => {
  assert.deepEqual(flatten([1, 2]), [1, 2]);
});
""",
            )
        ],
        "hidden_tests": [
            (
                "flatten_hidden",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { flatten } from "../solution.ts";

test("empty", () => {
  assert.deepEqual(flatten([]), []);
});
""",
            )
        ],
    },
    {
        "slug": "omit-keys-typescript",
        "title": "Omit Keys (TypeScript)",
        "difficulty": "easy",
        "description": "Return a shallow copy of `obj` without the listed `keys` (handbook-style utility).",
        "language": "typescript",
        "runtime": "5.7",
        "starter": """export function omit<T extends Record<string, unknown>>(
  obj: T,
  keys: (keyof T)[]
): Partial<T> {
  throw new Error("TODO");
}
""",
        "public_tests": [
            (
                "omit_public",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { omit } from "../solution.ts";

test("omits single key", () => {
  assert.deepEqual(omit({ a: 1, b: 2 }, ["b"]), { a: 1 });
});

test("omits multiple", () => {
  assert.deepEqual(omit({ x: 1, y: 2, z: 3 }, ["y", "z"]), { x: 1 });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "omit_hidden",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { omit } from "../solution.ts";

test("no keys", () => {
  assert.deepEqual(omit({ a: 1 }, []), { a: 1 });
});
""",
            )
        ],
    },
    {
        "slug": "pick-keys-typescript",
        "title": "Pick Keys (TypeScript)",
        "difficulty": "easy",
        "description": "Return a shallow object with only the selected keys from `obj`.",
        "language": "typescript",
        "runtime": "5.7",
        "starter": """export function pick<T extends Record<string, unknown>>(
  obj: T,
  keys: (keyof T)[]
): Partial<T> {
  throw new Error("TODO");
}
""",
        "public_tests": [
            (
                "pick_public",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { pick } from "../solution.ts";

test("picks keys", () => {
  assert.deepEqual(pick({ a: 1, b: 2, c: 3 }, ["a", "c"]), { a: 1, c: 3 });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "pick_hidden",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { pick } from "../solution.ts";

test("empty keys", () => {
  assert.deepEqual(pick({ a: 1 }, []), {});
});
""",
            )
        ],
    },
    {
        "slug": "unique-values-typescript",
        "title": "Unique Values (TypeScript)",
        "difficulty": "easy",
        "description": "Return array with duplicates removed (first occurrence order preserved).",
        "language": "typescript",
        "runtime": "5.7",
        "starter": "export function unique<T>(items: T[]): T[] {\n  throw new Error('TODO');\n}\n",
        "public_tests": [
            (
                "unique_public",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { unique } from "../solution.ts";

test("removes dupes", () => {
  assert.deepEqual(unique([1, 2, 2, 3, 1]), [1, 2, 3]);
});
""",
            )
        ],
        "hidden_tests": [
            (
                "unique_hidden",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { unique } from "../solution.ts";

test("strings", () => {
  assert.deepEqual(unique(["a", "b", "a"]), ["a", "b"]);
});
""",
            )
        ],
    },
    {
        "slug": "camel-case-typescript",
        "title": "Camel Case (TypeScript)",
        "difficulty": "easy",
        "description": "Convert `kebab-case` or `snake_case` identifiers to `camelCase`.",
        "language": "typescript",
        "runtime": "5.7",
        "starter": "export function toCamelCase(value: string): string {\n  throw new Error('TODO');\n}\n",
        "public_tests": [
            (
                "camel_public",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { toCamelCase } from "../solution.ts";

test("kebab to camel", () => {
  assert.equal(toCamelCase("foo-bar-baz"), "fooBarBaz");
});

test("snake to camel", () => {
  assert.equal(toCamelCase("hello_world"), "helloWorld");
});
""",
            )
        ],
        "hidden_tests": [
            (
                "camel_hidden",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { toCamelCase } from "../solution.ts";

test("single segment", () => {
  assert.equal(toCamelCase("item"), "item");
});
""",
            )
        ],
    },
    {
        "slug": "truncate-string-typescript",
        "title": "Truncate String (TypeScript)",
        "difficulty": "easy",
        "description": "If `text` length exceeds `max`, return prefix of length `max - suffix.length` plus `suffix`.",
        "language": "typescript",
        "runtime": "5.7",
        "starter": "export function truncate(text: string, max: number, suffix = \"...\"): string {\n  throw new Error('TODO');\n}\n",
        "public_tests": [
            (
                "truncate_public",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { truncate } from "../solution.ts";

test("truncates long text", () => {
  assert.equal(truncate("Hello World", 8), "Hello...");
});

test("short text unchanged", () => {
  assert.equal(truncate("Hi", 5), "Hi");
});
""",
            )
        ],
        "hidden_tests": [
            (
                "truncate_hidden",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { truncate } from "../solution.ts";

test("custom suffix", () => {
  assert.equal(truncate("abcdef", 5, "…"), "abcd…");
});
""",
            )
        ],
    },
    {
        "slug": "deep-equal-shallow-typescript",
        "title": "Deep Equal Shallow (TypeScript)",
        "difficulty": "medium",
        "description": "Return whether two plain objects have the same own keys and strictly equal values (one level).",
        "language": "typescript",
        "runtime": "5.7",
        "starter": "export function shallowEqual(a: Record<string, unknown>, b: Record<string, unknown>): boolean {\n  throw new Error('TODO');\n}\n",
        "public_tests": [
            (
                "shallow_equal_public",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { shallowEqual } from "../solution.ts";

test("equal objects", () => {
  assert.equal(shallowEqual({ a: 1, b: 2 }, { a: 1, b: 2 }), true);
});

test("different values", () => {
  assert.equal(shallowEqual({ a: 1 }, { a: 2 }), false);
});
""",
            )
        ],
        "hidden_tests": [
            (
                "shallow_equal_hidden",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { shallowEqual } from "../solution.ts";

test("different keys", () => {
  assert.equal(shallowEqual({ a: 1 }, { b: 1 }), false);
});
""",
            )
        ],
    },
    {
        "slug": "group-by-typescript",
        "title": "Group By (TypeScript)",
        "difficulty": "medium",
        "description": "Group array items by key returned from `selector` (TheAlgorithms / collection utility).",
        "language": "typescript",
        "runtime": "5.7",
        "starter": """export function groupBy<T>(
  items: T[],
  selector: (item: T) => string
): Record<string, T[]> {
  throw new Error("TODO");
}
""",
        "public_tests": [
            (
                "group_by_public",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { groupBy } from "../solution.ts";

test("groups by parity", () => {
  assert.deepEqual(
    groupBy([1, 2, 3, 4], (n) => (n % 2 === 0 ? "even" : "odd")),
    { odd: [1, 3], even: [2, 4] }
  );
});
""",
            )
        ],
        "hidden_tests": [
            (
                "group_by_hidden",
                """import { test } from "node:test";
import assert from "node:assert/strict";
import { groupBy } from "../solution.ts";

test("empty", () => {
  assert.deepEqual(groupBy([], (x) => String(x)), {});
});
""",
            )
        ],
    },
]
