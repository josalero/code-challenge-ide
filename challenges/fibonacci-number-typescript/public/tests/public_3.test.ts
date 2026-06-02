import { test } from "node:test";
import assert from "node:assert/strict";
import { fib } from "../solution.ts";

test("fibonacci(5) should equal 5", () => {
  assert.equal(fib(5), 5);
});
