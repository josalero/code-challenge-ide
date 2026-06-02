const { test } = require("node:test");
const assert = require("node:assert/strict");
const { twoSum } = require("../solution.js");

test("twoSum([2, 7, 11, 15], 9) should return [0, 1]", () => {
  assert.deepEqual(twoSum([2, 7, 11, 15], 9), [0, 1]);
});
