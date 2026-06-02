import { test } from "node:test";
import assert from "node:assert/strict";
import { mySqrt } from "../solution.ts";

test("integer square root of 8 should be 2", () => {
  assert.equal(mySqrt(8), 2);
});
