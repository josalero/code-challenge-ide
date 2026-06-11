const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isAnagram } = require("../solution.js");

test('isAnagram("rat", "car") should be false', () => {
  assert.equal(isAnagram("rat", "car"), false);
});
