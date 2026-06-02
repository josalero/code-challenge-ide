const { test } = require("node:test");
const assert = require("node:assert/strict");
const { reverseString } = require("../solution.js");

test("reverseString("race") should be "ecar"", () => {
  assert.equal(reverseString("race"), "ecar");
});
