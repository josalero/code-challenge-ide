const { test } = require("node:test");
const assert = require("node:assert/strict");
const { missingNumber } = require("../solution.js");

test("missingNumber([9, 6, 4, 2, 3, 5, 7, 0, 1]) should equal 8", () => {
  assert.equal(missingNumber([9, 6, 4, 2, 3, 5, 7, 0, 1]), 8);
});
