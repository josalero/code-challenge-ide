import { test } from "node:test";
import assert from "node:assert/strict";
import { chunk } from "../solution.ts";

test("chunks by two", () => {
  assert.deepEqual(chunk([1, 2, 3, 4, 5], 2), [[1, 2], [3, 4], [5]]);
});

test("size larger than array", () => {
  assert.deepEqual(chunk([1], 3), [[1]]);
});
