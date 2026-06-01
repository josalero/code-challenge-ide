import { test } from "node:test";
import assert from "node:assert/strict";
import { isPrime } from "../solution.ts";

test("isPrime(15) should be false", () => {
  assert.equal(isPrime(15), false);
});
