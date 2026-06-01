import { test } from "node:test";
import assert from "node:assert/strict";
import { isPrime } from "../solution.ts";

test("hidden", () => {
  assert.equal(isPrime(15), false);
  assert.equal(isPrime(97), true);
});
