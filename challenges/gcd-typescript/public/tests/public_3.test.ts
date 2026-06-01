import { test } from "node:test";
import assert from "node:assert/strict";
import { gcd } from "../solution.ts";

test("GCD(25, 15) should equal 5", () => {
  assert.equal(gcd(25, 15), 5);
});
