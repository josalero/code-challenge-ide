import { test } from "node:test";
import assert from "node:assert/strict";
import { maxSubArray } from "../solution.ts";

test("maxSubArray([-2, 1, -3, 4, -1, 2, 1, -4, 3]) should equal 6", () => {
  assert.equal(maxSubArray([-2, 1, -3, 4, -1, 2, 1, -4, 3]), 6);
});
