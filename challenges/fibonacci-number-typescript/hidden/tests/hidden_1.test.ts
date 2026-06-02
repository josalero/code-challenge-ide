import { test } from "node:test";
import assert from "node:assert/strict";
import { fib } from "../solution.ts";

test("fibonacci(10) should equal 55", () => {
  assert.equal(fib(10), 55);
});
