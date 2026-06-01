import { test } from "node:test";
import assert from "node:assert/strict";
import { fib } from "../solution.ts";

test("fibonacci(0) should equal 0", () => {
  assert.equal(fib(0), 0);
});
