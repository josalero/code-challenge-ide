import { test } from "node:test";
import assert from "node:assert/strict";
import { climbStairs } from "../solution.ts";

test("public", () => {
  assert.equal(climbStairs(2), 2);
  assert.equal(climbStairs(3), 3);
});
