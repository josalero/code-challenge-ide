import { test } from "node:test";
import assert from "node:assert/strict";
import { twoSum } from "../solution.ts";

test("twoSum([-1, -2, -3], -5) should return [1, 2]", () => {
  assert.deepEqual(twoSum([-1, -2, -3], -5), [1, 2]);
});
