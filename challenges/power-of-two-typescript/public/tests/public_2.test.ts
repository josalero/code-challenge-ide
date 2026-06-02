import { test } from "node:test";
import assert from "node:assert/strict";
import { isPowerOfTwo } from "../solution.ts";

test("isPowerOfTwo(3) should be false", () => {
  assert.equal(isPowerOfTwo(3), false);
});
