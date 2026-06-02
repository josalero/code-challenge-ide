const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isAnagram } = require("../solution.js");

test('isAnagram("anagram", "nagaram") should be true', () => {
  assert.equal(isAnagram("anagram", "nagaram"), true);
});
