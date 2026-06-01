const { test } = require("node:test");
const assert = require("node:assert/strict");
const { gcd } = require("../solution.js");

test("GCD(48, 18) should equal 12", () => {
  assert.equal(gcd(48, 18), 12);
});
