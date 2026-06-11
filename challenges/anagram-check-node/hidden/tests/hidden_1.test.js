const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isAnagram } = require("../solution.js");

test('isAnagram("a", "a") should be true", ', () => {
  assert.equal(isAnagram("a", "a"), true);
});
