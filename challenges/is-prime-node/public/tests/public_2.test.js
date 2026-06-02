const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isPrime } = require("../solution.js");

test("isPrime(2) should be true", () => {
  assert.equal(isPrime(2), true);
});
