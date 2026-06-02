const { test } = require("node:test");
const assert = require("node:assert/strict");
const { binarySearch } = require("../solution.js");

test("binarySearch([2, 4, 6], 2) should return index 0", () => {
  assert.equal(binarySearch([2, 4, 6], 2), 0);
});
