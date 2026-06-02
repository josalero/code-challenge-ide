const { test } = require("node:test");
const assert = require("node:assert/strict");
const { fib } = require("../solution.js");

test("fibonacci(0) should equal 0", () => {
  assert.equal(fib(0), 0);
});
