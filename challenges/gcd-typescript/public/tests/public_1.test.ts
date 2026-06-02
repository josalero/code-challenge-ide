import { test } from "node:test";
import assert from "node:assert/strict";
import { gcd } from "../solution.ts";

test("GCD(54, 24) should equal 6", () => {
  assert.equal(gcd(54, 24), 6);
});
