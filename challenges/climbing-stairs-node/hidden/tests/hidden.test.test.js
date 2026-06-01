const { test } = require("node:test");
const assert = require("node:assert/strict");
const { climbStairs } = require("../solution.js");

test("hidden", () => {
  assert.equal(climbStairs(10), 89);
  assert.equal(climbStairs(1), 1);
});
