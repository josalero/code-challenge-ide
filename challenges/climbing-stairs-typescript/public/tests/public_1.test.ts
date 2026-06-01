import { test } from "node:test";
import assert from "node:assert/strict";
import { climbStairs } from "../solution.ts";

test("climbStairs(2) should equal 2", () => {
  assert.equal(climbStairs(2), 2);
});
