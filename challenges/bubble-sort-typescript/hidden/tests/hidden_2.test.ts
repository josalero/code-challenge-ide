import { test } from "node:test";
import assert from "node:assert/strict";
import { bubbleSort } from "../solution.ts";

test("bubbleSort([5, 4, 3, 2, 1]) should return [1, 2, 3, 4, 5]", () => {
  assert.deepEqual(bubbleSort([5, 4, 3, 2, 1]), [1, 2, 3, 4, 5]);
});
