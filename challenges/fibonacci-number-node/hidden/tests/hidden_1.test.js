const { test } = require("node:test");
const assert = require("node:assert/strict");
const { fib } = require("../solution.js");

test("fibonacci(10) should equal 55", () => {
  assert.equal(fib(10), 55);
});
