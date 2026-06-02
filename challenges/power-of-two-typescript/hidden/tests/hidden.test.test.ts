import { test } from "node:test";
import assert from "node:assert/strict";
import { isPowerOfTwo } from "../solution.ts";

test("hidden", () => {
  assert.equal(isPowerOfTwo(16), true);
  assert.equal(isPowerOfTwo(0), false);
});
