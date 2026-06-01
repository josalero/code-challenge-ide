const { test } = require("node:test");
const assert = require("node:assert/strict");
const { climbStairs } = require("../solution.js");

test("climbStairs(10) should equal 89", () => {
  assert.equal(climbStairs(10), 89);
});
