const { test } = require("node:test");
const assert = require("node:assert/strict");
const { factorial } = require("../solution.js");

test("factorial(10) should equal 3628800", () => {
  assert.equal(factorial(10), 3628800);
});
