const { test } = require("node:test");
const assert = require("node:assert/strict");
const { fib } = require("../solution.js");

test("fibonacci(5) should equal 5", () => {
  assert.equal(fib(5), 5);
});
