const { test } = require("node:test");
const assert = require("node:assert/strict");
const { bubbleSort } = require("../solution.js");

test("bubbleSort([3, 1, 2]) should return [1, 2, 3]", () => {
  assert.deepEqual(bubbleSort([3, 1, 2]), [1, 2, 3]);
});
