const { test } = require("node:test");
const assert = require("node:assert/strict");
const { linearSearch } = require("../solution.js");

test("public", () => {
  assert.equal(linearSearch([2, 3, 4], 3), 1);
  assert.equal(linearSearch([1, 2], 5), -1);
});
