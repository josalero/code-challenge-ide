const { test } = require("node:test");
const assert = require("node:assert/strict");
const { linearSearch } = require("../solution.js");

test("linearSearch([9], 9) should return index 0", () => {
  assert.equal(linearSearch([9], 9), 0);
});
