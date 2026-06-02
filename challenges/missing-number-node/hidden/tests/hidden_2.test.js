const { test } = require("node:test");
const assert = require("node:assert/strict");
const { missingNumber } = require("../solution.js");

test("missingNumber([1]) should equal 0", () => {
  assert.equal(missingNumber([1]), 0);
});
