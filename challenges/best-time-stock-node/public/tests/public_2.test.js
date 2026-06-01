const { test } = require("node:test");
const assert = require("node:assert/strict");
const { maxProfit } = require("../solution.js");

test("maxProfit([7, 6, 4, 3, 1]) should equal 0", () => {
  assert.equal(maxProfit([7, 6, 4, 3, 1]), 0);
});
