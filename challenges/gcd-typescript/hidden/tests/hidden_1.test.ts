import { test } from "node:test";
import assert from "node:assert/strict";
import { gcd } from "../solution.ts";

test("GCD(48, 18) should equal 12", () => {
  assert.equal(gcd(48, 18), 12);
});
