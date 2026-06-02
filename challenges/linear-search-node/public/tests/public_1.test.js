const { test } = require("node:test");
const assert = require("node:assert/strict");
const { linearSearch } = require("../solution.js");

test("linearSearch([2, 3, 4], 3) should return index 1", () => {
  assert.equal(linearSearch([2, 3, 4], 3), 1);
});
