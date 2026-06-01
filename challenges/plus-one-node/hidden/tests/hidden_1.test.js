const { test } = require("node:test");
const assert = require("node:assert/strict");
const { plusOne } = require("../solution.js");

test("plusOne([0]) should return [1]", () => {
  assert.deepEqual(plusOne([0]), [1]);
});
