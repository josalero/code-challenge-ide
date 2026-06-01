const { test } = require("node:test");
const assert = require("node:assert/strict");
const { mySqrt } = require("../solution.js");

test("integer square root of 8 should be 2", () => {
  assert.equal(mySqrt(8), 2);
});
