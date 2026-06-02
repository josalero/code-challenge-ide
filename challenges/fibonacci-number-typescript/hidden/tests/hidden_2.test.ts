import { test } from "node:test";
import assert from "node:assert/strict";
import { fib } from "../solution.ts";

test("fibonacci(6) should equal 8", () => {
  assert.equal(fib(6), 8);
});
