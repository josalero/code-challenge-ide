import { test } from "node:test";
import assert from "node:assert/strict";
import { mySqrt } from "../solution.ts";

test("integer square root of 2147483647 should be 46340", () => {
  assert.equal(mySqrt(2147483647), 46340);
});
