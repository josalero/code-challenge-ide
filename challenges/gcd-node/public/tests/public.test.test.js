const { test } = require("node:test");
const assert = require("node:assert/strict");
const { gcd } = require("../solution.js");

test("public", () => {
  assert.equal(gcd(54, 24), 6);
  assert.equal(gcd(17, 13), 1);
  assert.equal(gcd(25, 15), 5);
});
