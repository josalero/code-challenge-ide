const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isPowerOfTwo } = require("../solution.js");

test("hidden", () => {
  assert.equal(isPowerOfTwo(16), true);
  assert.equal(isPowerOfTwo(0), false);
});
