import { test } from "node:test";
import assert from "node:assert/strict";
import { isPrime } from "../solution.ts";

test("isPrime(1) should be false", () => {
  assert.equal(isPrime(1), false);
});
