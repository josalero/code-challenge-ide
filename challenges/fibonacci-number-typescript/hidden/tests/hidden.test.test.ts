import { test } from "node:test";
import assert from "node:assert/strict";
import { fib } from "../solution.ts";

test("hidden", () => {
  assert.equal(fib(10), 55);
  assert.equal(fib(6), 8);
});
