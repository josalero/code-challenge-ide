const { test } = require("node:test");
const assert = require("node:assert/strict");
const { singleNumber } = require("../solution.js");

test("singleNumber([6, 3, 6]) should equal 3", () => {
  assert.equal(singleNumber([6, 3, 6]), 3);
});
