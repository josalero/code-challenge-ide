const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isValidParentheses } = require("../solution.js");

test("isValid("()") should be true", () => {
  assert.equal(isValidParentheses("()"), true);
});
