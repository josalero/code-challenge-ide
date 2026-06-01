const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isPowerOfTwo } = require("../solution.js");

test("public", () => {
  assert.equal(isPowerOfTwo(1), true);
  assert.equal(isPowerOfTwo(3), false);
});
