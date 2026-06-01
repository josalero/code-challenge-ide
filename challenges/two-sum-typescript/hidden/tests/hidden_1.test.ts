import { test } from "node:test";
import assert from "node:assert/strict";
import { twoSum } from "../solution.ts";

test("twoSum([3, 3], 6) should return [0, 1]", () => {
  assert.deepEqual(twoSum([3, 3], 6), [0, 1]);
});
