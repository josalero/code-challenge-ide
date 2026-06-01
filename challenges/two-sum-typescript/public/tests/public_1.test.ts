import { test } from "node:test";
import assert from "node:assert/strict";
import { twoSum } from "../solution.ts";

test("twoSum([2, 7, 11, 15], 9) should return [0, 1]", () => {
  assert.deepEqual(twoSum([2, 7, 11, 15], 9), [0, 1]);
});
