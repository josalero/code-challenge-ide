import { test } from "node:test";
import assert from "node:assert/strict";
import { climbStairs } from "../solution.ts";

test("climbStairs(3) should equal 3", () => {
  assert.equal(climbStairs(3), 3);
});
