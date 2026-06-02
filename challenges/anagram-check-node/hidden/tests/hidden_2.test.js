const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isAnagram } = require("../solution.js");

test('isAnagram("ab", "a") should be false', () => {
  assert.equal(isAnagram("ab", "a"), false);
});
