const { test } = require("node:test");
const assert = require("node:assert/strict");
const { fib } = require("../solution.js");

test("public", () => {
  assert.equal(fib(0), 0);
  assert.equal(fib(1), 1);
  assert.equal(fib(5), 5);
});
