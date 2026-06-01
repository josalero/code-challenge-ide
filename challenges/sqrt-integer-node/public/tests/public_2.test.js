const { test } = require("node:test");
const assert = require("node:assert/strict");
const { mySqrt } = require("../solution.js");

test("integer square root of 0 should be 0", () => {
  assert.equal(mySqrt(0), 0);
});
