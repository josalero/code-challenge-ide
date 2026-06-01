const { test } = require("node:test");
const assert = require("node:assert/strict");
const { factorial } = require("../solution.js");

test("hidden", () => {
  assert.equal(factorial(10), 3628800);
  assert.equal(factorial(3), 6);
});
