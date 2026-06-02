import { test } from "node:test";
import assert from "node:assert/strict";
import { mySqrt } from "../solution.ts";

test("integer square root of 10 should be 3", () => {
  assert.equal(mySqrt(10), 3);
});
