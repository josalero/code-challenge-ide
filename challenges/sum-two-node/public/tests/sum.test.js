const { test } = require("node:test");
const assert = require("node:assert/strict");
const { sum } = require("../solution.js");

test("adds small numbers", () => {
  assert.equal(sum(2, 3), 5);
});

test("adds negatives", () => {
  assert.equal(sum(-1, 1), 0);
});
