const { test } = require("node:test");
const assert = require("node:assert/strict");
const { gcd } = require("../solution.js");

test("GCD(0, 7) should equal 7", () => {
  assert.equal(gcd(0, 7), 7);
});
