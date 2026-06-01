const { test } = require("node:test");
const assert = require("node:assert/strict");
const { factorial } = require("../solution.js");

test("factorial(1) should equal 1", () => {
  assert.equal(factorial(1), 1);
});
