import { test } from "node:test";
import assert from "node:assert/strict";
import { mySqrt } from "../solution.ts";

test("public", () => {
  assert.equal(mySqrt(8), 2);
  assert.equal(mySqrt(0), 0);
});
