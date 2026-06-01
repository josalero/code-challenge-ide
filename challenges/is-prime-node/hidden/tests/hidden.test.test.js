const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isPrime } = require("../solution.js");

test("hidden", () => {
  assert.equal(isPrime(15), false);
  assert.equal(isPrime(97), true);
});
