const { test } = require("node:test");
const assert = require("node:assert/strict");
const { twoSum } = require("../solution.js");

test("twoSum([-1, -2, -3], -5) should return [1, 2]", () => {
  assert.deepEqual(twoSum([-1, -2, -3], -5), [1, 2]);
});
