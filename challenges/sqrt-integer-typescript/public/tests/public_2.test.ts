import { test } from "node:test";
import assert from "node:assert/strict";
import { mySqrt } from "../solution.ts";

test("integer square root of 0 should be 0", () => {
  assert.equal(mySqrt(0), 0);
});
