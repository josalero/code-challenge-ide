const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isPowerOfTwo } = require("../solution.js");

test("isPowerOfTwo(16) should be true", () => {
  assert.equal(isPowerOfTwo(16), true);
});
