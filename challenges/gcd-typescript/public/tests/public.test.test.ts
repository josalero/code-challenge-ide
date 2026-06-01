import { test } from "node:test";
import assert from "node:assert/strict";
import { gcd } from "../solution.ts";

test("public", () => {
  assert.equal(gcd(54, 24), 6);
  assert.equal(gcd(17, 13), 1);
  assert.equal(gcd(25, 15), 5);
});
