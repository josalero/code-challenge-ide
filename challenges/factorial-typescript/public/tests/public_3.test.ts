import { test } from "node:test";
import assert from "node:assert/strict";
import { factorial } from "../solution.ts";

test("factorial(1) should equal 1", () => {
  assert.equal(factorial(1), 1);
});
