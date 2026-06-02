const { test } = require("node:test");
const assert = require("node:assert/strict");
const { containsDuplicate } = require("../solution.js");

test("containsDuplicate([1, 1]) should be true", () => {
  assert.equal(containsDuplicate([1, 1]), true);
});
