import { test } from "node:test";
import assert from "node:assert/strict";
import { twoSum } from "../solution.ts";

test("twoSum([3, 2, 4], 6) should return [1, 2]", () => {
  assert.deepEqual(twoSum([3, 2, 4], 6), [1, 2]);
});
