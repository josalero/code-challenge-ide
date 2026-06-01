const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isPowerOfTwo } = require("../solution.js");

test("isPowerOfTwo(0) should be false", () => {
  assert.equal(isPowerOfTwo(0), false);
});
