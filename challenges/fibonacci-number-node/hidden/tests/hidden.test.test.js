const { test } = require("node:test");
const assert = require("node:assert/strict");
const { fib } = require("../solution.js");

test("hidden", () => {
  assert.equal(fib(10), 55);
  assert.equal(fib(6), 8);
});
