import { test } from "node:test";
import assert from "node:assert/strict";
import { isPrime } from "../solution.ts";

test("public", () => {
  assert.equal(isPrime(1), false);
  assert.equal(isPrime(2), true);
  assert.equal(isPrime(17), true);
});
