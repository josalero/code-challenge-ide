import { test } from "node:test";
import assert from "node:assert/strict";
import { gcd } from "../solution.ts";

test("GCD(17, 13) should equal 1", () => {
  assert.equal(gcd(17, 13), 1);
});
