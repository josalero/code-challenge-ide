import { test } from "node:test";
import assert from "node:assert/strict";
import { fib } from "../solution.ts";

test("public", () => {
  assert.equal(fib(0), 0);
  assert.equal(fib(1), 1);
  assert.equal(fib(5), 5);
});
