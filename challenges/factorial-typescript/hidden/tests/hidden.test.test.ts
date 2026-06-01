import { test } from "node:test";
import assert from "node:assert/strict";
import { factorial } from "../solution.ts";

test("hidden", () => {
  assert.equal(factorial(10), 3628800);
  assert.equal(factorial(3), 6);
});
