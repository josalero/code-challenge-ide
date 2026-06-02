const { test } = require("node:test");
const assert = require("node:assert/strict");
const { factorial } = require("../solution.js");

test("public", () => {
  assert.equal(factorial(0), 1);
  assert.equal(factorial(5), 120);
  assert.equal(factorial(1), 1);
});
