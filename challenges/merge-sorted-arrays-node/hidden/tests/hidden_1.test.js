const { test } = require("node:test");
const assert = require("node:assert/strict");
const { mergeSorted } = require("../solution.js");

test("mergeSorted([1], []) should return [1]", () => {
  assert.deepEqual(mergeSorted([1], []), [1]);
});
