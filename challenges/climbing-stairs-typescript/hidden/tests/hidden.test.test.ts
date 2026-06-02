import { test } from "node:test";
import assert from "node:assert/strict";
import { climbStairs } from "../solution.ts";

test("hidden", () => {
  assert.equal(climbStairs(10), 89);
  assert.equal(climbStairs(1), 1);
});
