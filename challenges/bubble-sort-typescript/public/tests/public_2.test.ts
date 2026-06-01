import { test } from "node:test";
import assert from "node:assert/strict";
import { bubbleSort } from "../solution.ts";

test("bubbleSort([1]) should return [1]", () => {
  assert.deepEqual(bubbleSort([1]), [1]);
});
