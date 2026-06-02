import { test } from "node:test";
import assert from "node:assert/strict";
import { sum } from "../solution.ts";

test("adds zero", () => {
  assert.equal(sum(0, 0), 0);
});

test("adds large values", () => {
  assert.equal(sum(1000, 2000), 3000);
});
