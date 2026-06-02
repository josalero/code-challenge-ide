const { test } = require("node:test");
const assert = require("node:assert/strict");
const { maxSubArray } = require("../solution.js");

test("maxSubArray([1]) should equal 1", () => {
  assert.equal(maxSubArray([1]), 1);
});
