import { test } from "node:test";
import assert from "node:assert/strict";
import { factorial } from "../solution.ts";

test("public", () => {
  assert.equal(factorial(0), 1);
  assert.equal(factorial(5), 120);
  assert.equal(factorial(1), 1);
});
