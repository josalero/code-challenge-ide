import { test } from "node:test";
import assert from "node:assert/strict";
import { factorial } from "../solution.ts";

test("factorial(5) should equal 120", () => {
  assert.equal(factorial(5), 120);
});
