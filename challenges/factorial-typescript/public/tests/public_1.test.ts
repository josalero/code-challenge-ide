import { test } from "node:test";
import assert from "node:assert/strict";
import { factorial } from "../solution.ts";

test("factorial(0) should equal 1", () => {
  assert.equal(factorial(0), 1);
});
