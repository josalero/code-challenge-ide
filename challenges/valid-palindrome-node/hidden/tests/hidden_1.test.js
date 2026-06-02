const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isPalindrome } = require("../solution.js");

test("isPalindrome("") should be true", () => {
  assert.equal(isPalindrome(""), true);
});
