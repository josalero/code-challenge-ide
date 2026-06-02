import { test } from "node:test";
import assert from "node:assert/strict";
import { fib } from "../solution.ts";

test("fibonacci(1) should equal 1", () => {
  assert.equal(fib(1), 1);
});
