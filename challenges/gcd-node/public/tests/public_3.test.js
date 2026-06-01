const { test } = require("node:test");
const assert = require("node:assert/strict");
const { gcd } = require("../solution.js");

test("GCD(25, 15) should equal 5", () => {
  assert.equal(gcd(25, 15), 5);
});
