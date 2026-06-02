const { test } = require("node:test");
const assert = require("node:assert/strict");
const { bubbleSort } = require("../solution.js");

test("bubbleSort([]) should return []", () => {
  assert.deepEqual(bubbleSort([]), []);
});
