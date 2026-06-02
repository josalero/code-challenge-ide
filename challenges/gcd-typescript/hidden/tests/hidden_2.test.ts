import { test } from "node:test";
import assert from "node:assert/strict";
import { gcd } from "../solution.ts";

test("GCD(0, 7) should equal 7", () => {
  assert.equal(gcd(0, 7), 7);
});
