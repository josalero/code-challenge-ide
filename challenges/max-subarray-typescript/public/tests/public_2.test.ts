import { test } from "node:test";
import assert from "node:assert/strict";
import { maxSubArray } from "../solution.ts";

test("maxSubArray([1]) should equal 1", () => {
  assert.equal(maxSubArray([1]), 1);
});
