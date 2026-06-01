const { test } = require("node:test");
const assert = require("node:assert/strict");
const { factorial } = require("../solution.js");

test("factorial(3) should equal 6", () => {
  assert.equal(factorial(3), 6);
});
