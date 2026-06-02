const { test } = require("node:test");
const assert = require("node:assert/strict");
const { gcd } = require("../solution.js");

test("hidden", () => {
  assert.equal(gcd(48, 18), 12);
  assert.equal(gcd(0, 7), 7);
});
