const { test } = require("node:test");
const assert = require("node:assert/strict");
const { containsDuplicate } = require("../solution.js");

test("hidden", () => {
  assert.equal(containsDuplicate([1, 1]), true);
  assert.equal(containsDuplicate([]), false);
});
