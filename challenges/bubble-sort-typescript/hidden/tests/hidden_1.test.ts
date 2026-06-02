import { test } from "node:test";
import assert from "node:assert/strict";
import { bubbleSort } from "../solution.ts";

test("bubbleSort([]) should return []", () => {
  assert.deepEqual(bubbleSort([]), []);
});
