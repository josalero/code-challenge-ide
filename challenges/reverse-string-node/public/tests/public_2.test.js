const { test } = require("node:test");
const assert = require("node:assert/strict");
const { reverseString } = require("../solution.js");

test("reverseString("") should be """, () => {
  assert.equal(reverseString(""), "");
});
