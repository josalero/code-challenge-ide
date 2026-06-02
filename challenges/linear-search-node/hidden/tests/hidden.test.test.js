const { test } = require("node:test");
const assert = require("node:assert/strict");
const { linearSearch } = require("../solution.js");

test("hidden", () => {
  assert.equal(linearSearch([9], 9), 0);
  assert.equal(linearSearch([], 1), -1);
});
