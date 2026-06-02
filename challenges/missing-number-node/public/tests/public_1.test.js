const { test } = require("node:test");
const assert = require("node:assert/strict");
const { missingNumber } = require("../solution.js");

test("missingNumber([3, 0, 1]) should equal 2", () => {
  assert.equal(missingNumber([3, 0, 1]), 2);
});
