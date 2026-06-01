import { test } from "node:test";
import assert from "node:assert/strict";
import { gcd } from "../solution.ts";

test("hidden", () => {
  assert.equal(gcd(48, 18), 12);
  assert.equal(gcd(0, 7), 7);
});
