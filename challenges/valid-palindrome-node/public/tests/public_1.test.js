const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isPalindrome } = require("../solution.js");

test("isPalindrome("A man, a plan, a can…") should be true", () => {
  assert.equal(isPalindrome("A man, a plan, a canal: Panama"), true);
});
