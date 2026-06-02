const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isPrime } = require("../solution.js");

test("isPrime(15) should be false", () => {
  assert.equal(isPrime(15), false);
});
