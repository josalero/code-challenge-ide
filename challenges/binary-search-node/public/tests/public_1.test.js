const { test } = require("node:test");
const assert = require("node:assert/strict");
const { binarySearch } = require("../solution.js");

test("binarySearch([1, 3, 5, 7, 9], 3) should return index 1", () => {
  assert.equal(binarySearch([1, 3, 5, 7, 9], 3), 1);
});
