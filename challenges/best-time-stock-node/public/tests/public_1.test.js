const { test } = require("node:test");
const assert = require("node:assert/strict");
const { maxProfit } = require("../solution.js");

test("maxProfit([7, 1, 5, 3, 6, 4]) should equal 5", () => {
  assert.equal(maxProfit([7, 1, 5, 3, 6, 4]), 5);
});
