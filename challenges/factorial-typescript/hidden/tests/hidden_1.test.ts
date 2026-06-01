import { test } from "node:test";
import assert from "node:assert/strict";
import { factorial } from "../solution.ts";

test("factorial(10) should equal 3628800", () => {
  assert.equal(factorial(10), 3628800);
});
