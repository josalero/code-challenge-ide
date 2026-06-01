import { test } from "node:test";
import assert from "node:assert/strict";
import { shallowEqual } from "../solution.ts";

test("equal objects", () => {
  assert.equal(shallowEqual({ a: 1, b: 2 }, { a: 1, b: 2 }), true);
});

test("different values", () => {
  assert.equal(shallowEqual({ a: 1 }, { a: 2 }), false);
});
