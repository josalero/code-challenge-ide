const { test } = require("node:test");
const assert = require("node:assert/strict");
const { missingNumber } = require("../solution.js");

test("public", () => {
  assert.equal(missingNumber([3, 0, 1]), 2);
  assert.equal(missingNumber([0]), 1);
});
