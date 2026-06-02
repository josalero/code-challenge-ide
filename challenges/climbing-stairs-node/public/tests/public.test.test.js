const { test } = require("node:test");
const assert = require("node:assert/strict");
const { climbStairs } = require("../solution.js");

test("public", () => {
  assert.equal(climbStairs(2), 2);
  assert.equal(climbStairs(3), 3);
});
