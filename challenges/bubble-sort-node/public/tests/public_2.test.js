const { test } = require("node:test");
const assert = require("node:assert/strict");
const { bubbleSort } = require("../solution.js");

test("bubbleSort([1]) should return [1]", () => {
  assert.deepEqual(bubbleSort([1]), [1]);
});
