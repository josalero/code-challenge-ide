import { test } from "node:test";
import assert from "node:assert/strict";
import { bubbleSort } from "../solution.ts";

test("bubbleSort([3, 1, 2]) should return [1, 2, 3]", () => {
  assert.deepEqual(bubbleSort([3, 1, 2]), [1, 2, 3]);
});
