const { test } = require("node:test");
const assert = require("node:assert/strict");
const { sum } = require("../solution.js");

test("adds zero", () => {
  assert.equal(sum(0, 0), 0);
});

test("adds large values", () => {
  assert.equal(sum(1000, 2000), 3000);
});
