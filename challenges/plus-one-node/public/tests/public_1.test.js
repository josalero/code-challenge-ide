const { test } = require("node:test");
const assert = require("node:assert/strict");
const { plusOne } = require("../solution.js");

test("plusOne([1, 2, 3]) should return [1, 2, 4]", () => {
  assert.deepEqual(plusOne([1, 2, 3]), [1, 2, 4]);
});
