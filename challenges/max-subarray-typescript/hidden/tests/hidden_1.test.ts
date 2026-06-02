import { test } from "node:test";
import assert from "node:assert/strict";
import { maxSubArray } from "../solution.ts";

test("maxSubArray([5, 4, -1, 7, 8]) should equal 23", () => {
  assert.equal(maxSubArray([5, 4, -1, 7, 8]), 23);
});
