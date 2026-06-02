const { test } = require("node:test");
const assert = require("node:assert/strict");
const { containsDuplicate } = require("../solution.js");

test("containsDuplicate([1, 2, 3, 4]) should be false", () => {
  assert.equal(containsDuplicate([1, 2, 3, 4]), false);
});
