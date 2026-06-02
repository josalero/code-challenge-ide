const { test } = require("node:test");
const assert = require("node:assert/strict");
const { twoSum } = require("../solution.js");

test("twoSum([3, 3], 6) should return [0, 1]", () => {
  assert.deepEqual(twoSum([3, 3], 6), [0, 1]);
});
