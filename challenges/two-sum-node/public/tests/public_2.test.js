const { test } = require("node:test");
const assert = require("node:assert/strict");
const { twoSum } = require("../solution.js");

test("twoSum([3, 2, 4], 6) should return [1, 2]", () => {
  assert.deepEqual(twoSum([3, 2, 4], 6), [1, 2]);
});
