const { test } = require("node:test");
const assert = require("node:assert/strict");
const { bubbleSort } = require("../solution.js");

test("bubbleSort([5, 4, 3, 2, 1]) should return [1, 2, 3, 4, 5]", () => {
  assert.deepEqual(bubbleSort([5, 4, 3, 2, 1]), [1, 2, 3, 4, 5]);
});
