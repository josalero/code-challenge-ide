const { test } = require("node:test");
const assert = require("node:assert/strict");
const { singleNumber } = require("../solution.js");

test("singleNumber([2, 2, 1]) should equal 1", () => {
  assert.equal(singleNumber([2, 2, 1]), 1);
});
