const { test } = require("node:test");
const assert = require("node:assert/strict");
const { factorial } = require("../solution.js");

test("factorial(5) should equal 120", () => {
  assert.equal(factorial(5), 120);
});
