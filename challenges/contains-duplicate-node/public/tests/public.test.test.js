const { test } = require("node:test");
const assert = require("node:assert/strict");
const { containsDuplicate } = require("../solution.js");

test("public", () => {
  assert.equal(containsDuplicate([1, 2, 3, 1]), true);
  assert.equal(containsDuplicate([1, 2, 3, 4]), false);
});
