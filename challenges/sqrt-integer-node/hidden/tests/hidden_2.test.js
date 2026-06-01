const { test } = require("node:test");
const assert = require("node:assert/strict");
const { mySqrt } = require("../solution.js");

test("integer square root of 2147483647 should be 46340", () => {
  assert.equal(mySqrt(2147483647), 46340);
});
