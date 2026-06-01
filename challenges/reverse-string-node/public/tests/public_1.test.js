const { test } = require("node:test");
const assert = require("node:assert/strict");
const { reverseString } = require("../solution.js");

test("reverseString("hello") should be "olleh"", () => {
  assert.equal(reverseString("hello"), "olleh");
});
