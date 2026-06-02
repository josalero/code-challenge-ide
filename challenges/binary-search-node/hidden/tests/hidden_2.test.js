const { test } = require("node:test");
const assert = require("node:assert/strict");
const { binarySearch } = require("../solution.js");

test("binarySearch([], 1) should return index -1", () => {
  assert.equal(binarySearch([], 1), -1);
});
