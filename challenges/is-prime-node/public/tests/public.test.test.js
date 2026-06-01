const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isPrime } = require("../solution.js");

test("public", () => {
  assert.equal(isPrime(1), false);
  assert.equal(isPrime(2), true);
  assert.equal(isPrime(17), true);
});
