const { test } = require("node:test");
const assert = require("node:assert/strict");
const { mySqrt } = require("../solution.js");

test("integer square root of 10 should be 3", () => {
  assert.equal(mySqrt(10), 3);
});
