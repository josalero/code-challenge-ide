const { test } = require("node:test");
const assert = require("node:assert/strict");
const { containsDuplicate } = require("../solution.js");

test("containsDuplicate([]) should be false", () => {
  assert.equal(containsDuplicate([]), false);
});
