import { test } from "node:test";
import assert from "node:assert/strict";
import { isPrime } from "../solution.ts";

test("isPrime(17) should be true", () => {
  assert.equal(isPrime(17), true);
});
