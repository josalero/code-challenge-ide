const { test } = require("node:test");
const assert = require("node:assert/strict");
const { fib } = require("../solution.js");

test("fibonacci(6) should equal 8", () => {
  assert.equal(fib(6), 8);
});
