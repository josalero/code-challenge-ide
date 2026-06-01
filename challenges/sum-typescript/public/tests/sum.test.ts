import { test } from "node:test";
import assert from "node:assert/strict";
import { sum } from "../solution.ts";

test("adds small numbers", () => {
  assert.equal(sum(2, 3), 5);
});

test("adds negatives", () => {
  assert.equal(sum(-1, 1), 0);
});
