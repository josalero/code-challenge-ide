const { test } = require("node:test");
const assert = require("node:assert/strict");
const { mySqrt } = require("../solution.js");

test("public", () => {
  assert.equal(mySqrt(8), 2);
  assert.equal(mySqrt(0), 0);
});
