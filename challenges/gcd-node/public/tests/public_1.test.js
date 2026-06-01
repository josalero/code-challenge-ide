const { test } = require("node:test");
const assert = require("node:assert/strict");
const { gcd } = require("../solution.js");

test("GCD(54, 24) should equal 6", () => {
  assert.equal(gcd(54, 24), 6);
});
