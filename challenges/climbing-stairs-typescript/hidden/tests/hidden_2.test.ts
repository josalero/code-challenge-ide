import { test } from "node:test";
import assert from "node:assert/strict";
import { climbStairs } from "../solution.ts";

test("climbStairs(1) should equal 1", () => {
  assert.equal(climbStairs(1), 1);
});
