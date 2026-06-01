const { test } = require("node:test");
const assert = require("node:assert/strict");
const { maxSubArray } = require("../solution.js");

test("maxSubArray([5, 4, -1, 7, 8]) should equal 23", () => {
  assert.equal(maxSubArray([5, 4, -1, 7, 8]), 23);
});
