const { test } = require("node:test");
const assert = require("node:assert/strict");
const { maxProfit } = require("../solution.js");

test("maxProfit([1, 2]) should equal 1", () => {
  assert.equal(maxProfit([1, 2]), 1);
});
