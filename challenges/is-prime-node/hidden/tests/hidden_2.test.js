const { test } = require("node:test");
const assert = require("node:assert/strict");
const { isPrime } = require("../solution.js");

test("isPrime(97) should be true", () => {
  assert.equal(isPrime(97), true);
});
