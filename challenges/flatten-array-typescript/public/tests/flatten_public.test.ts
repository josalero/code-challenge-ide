import { test } from "node:test";
import assert from "node:assert/strict";
import { flatten } from "../solution.ts";

test("flattens one level", () => {
  assert.deepEqual(flatten([1, [2, 3], 4]), [1, 2, 3, 4]);
});

test("already flat", () => {
  assert.deepEqual(flatten([1, 2]), [1, 2]);
});
