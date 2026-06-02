const { test } = require("node:test");
const assert = require("node:assert/strict");
const { plusOne } = require("../solution.js");

test("plusOne([9]) should return [1, 0]", () => {
  assert.deepEqual(plusOne([9]), [1, 0]);
});
