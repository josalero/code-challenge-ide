const { test } = require("node:test");
const assert = require("node:assert/strict");
const { maxSubArray } = require("../solution.js");

test("maxSubArray([-2, 1, -3, 4, -1, 2, 1, -4, 3]) should equal 6", () => {
  assert.equal(maxSubArray([-2, 1, -3, 4, -1, 2, 1, -4, 3]), 6);
});
