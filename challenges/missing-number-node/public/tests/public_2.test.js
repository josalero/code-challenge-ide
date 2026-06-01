const { test } = require("node:test");
const assert = require("node:assert/strict");
const { missingNumber } = require("../solution.js");

test("missingNumber([0]) should equal 1", () => {
  assert.equal(missingNumber([0]), 1);
});
