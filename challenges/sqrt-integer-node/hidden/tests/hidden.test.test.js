const { test } = require("node:test");
const assert = require("node:assert/strict");
const { mySqrt } = require("../solution.js");

test("hidden", () => {
  assert.equal(mySqrt(10), 3);
  assert.equal(mySqrt(2147483647), 46340);
});
