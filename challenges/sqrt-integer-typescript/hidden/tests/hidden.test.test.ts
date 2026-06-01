import { test } from "node:test";
import assert from "node:assert/strict";
import { mySqrt } from "../solution.ts";

test("hidden", () => {
  assert.equal(mySqrt(10), 3);
  assert.equal(mySqrt(2147483647), 46340);
});
