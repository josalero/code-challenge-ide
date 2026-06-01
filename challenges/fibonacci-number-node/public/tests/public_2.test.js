const { test } = require("node:test");
const assert = require("node:assert/strict");
const { fib } = require("../solution.js");

test("fibonacci(1) should equal 1", () => {
  assert.equal(fib(1), 1);
});
