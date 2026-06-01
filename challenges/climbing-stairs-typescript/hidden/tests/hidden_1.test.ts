import { test } from "node:test";
import assert from "node:assert/strict";
import { climbStairs } from "../solution.ts";

test("climbStairs(10) should equal 89", () => {
  assert.equal(climbStairs(10), 89);
});
