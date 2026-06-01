const { test } = require("node:test");
const assert = require("node:assert/strict");
const { mergeSorted } = require("../solution.js");

test("mergeSorted([1, 2, 3], [2, 5, 6]) should return [1, 2, 2, 3, 5, 6]", () => {
  assert.deepEqual(mergeSorted([1, 2, 3], [2, 5, 6]), [1, 2, 2, 3, 5, 6]);
});
