import { test } from "node:test";
import assert from "node:assert/strict";
import { isPowerOfTwo } from "../solution.ts";

test("isPowerOfTwo(0) should be false", () => {
  assert.equal(isPowerOfTwo(0), false);
});
