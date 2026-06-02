const { test } = require("node:test");
const assert = require("node:assert/strict");
const { climbStairs } = require("../solution.js");

test("climbStairs(3) should equal 3", () => {
  assert.equal(climbStairs(3), 3);
});
