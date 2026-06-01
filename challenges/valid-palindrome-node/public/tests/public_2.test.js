const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isPalindrome } = require("../solution.js");

test("isPalindrome("race a car") should be false", () => {
  assert.equal(isPalindrome("race a car"), false);
});
