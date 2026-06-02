const { test } = require("node:test");
const assert = require("node:assert/strict");
const { climbStairs } = require("../solution.js");

test("climbStairs(2) should equal 2", () => {
  assert.equal(climbStairs(2), 2);
});
