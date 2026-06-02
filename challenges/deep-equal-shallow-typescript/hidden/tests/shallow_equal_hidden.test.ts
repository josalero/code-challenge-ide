import { test } from "node:test";
import assert from "node:assert/strict";
import { shallowEqual } from "../solution.ts";

test("different keys", () => {
  assert.equal(shallowEqual({ a: 1 }, { b: 1 }), false);
});
