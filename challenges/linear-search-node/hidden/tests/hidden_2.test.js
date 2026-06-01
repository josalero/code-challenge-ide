const { test } = require("node:test");
const assert = require("node:assert/strict");
const { linearSearch } = require("../solution.js");

test("linearSearch([], 1) should return index -1", () => {
  assert.equal(linearSearch([], 1), -1);
});
