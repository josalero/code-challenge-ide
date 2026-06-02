const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isPowerOfTwo } = require("../solution.js");

test("isPowerOfTwo(3) should be false", () => {
  assert.equal(isPowerOfTwo(3), false);
});
