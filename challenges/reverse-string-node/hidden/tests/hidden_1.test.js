const { test } = require("node:test");
const assert = require("node:assert/strict");
const { reverseString } = require("../solution.js");

test("reverseString("a") should be "a"", () => {
  assert.equal(reverseString("a"), "a");
});
