const { test } = require("node:test");
const assert = require("node:assert/strict");
const { singleNumber } = require("../solution.js");

test("singleNumber([4, 1, 2, 1, 2]) should equal 4", () => {
  assert.equal(singleNumber([4, 1, 2, 1, 2]), 4);
});
