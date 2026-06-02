const { test } = require("node:test");
const assert = require("node:assert/strict");
const { climbStairs } = require("../solution.js");

test("climbStairs(1) should equal 1", () => {
  assert.equal(climbStairs(1), 1);
});
