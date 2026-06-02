const { test } = require("node:test");
const assert = require("node:assert/strict");
const { maxProfit } = require("../solution.js");

test("maxProfit([2, 4, 1]) should equal 2", () => {
  assert.equal(maxProfit([2, 4, 1]), 2);
});
