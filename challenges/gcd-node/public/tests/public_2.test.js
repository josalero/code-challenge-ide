const { test } = require("node:test");
const assert = require("node:assert/strict");
const { gcd } = require("../solution.js");

test("GCD(17, 13) should equal 1", () => {
  assert.equal(gcd(17, 13), 1);
});
