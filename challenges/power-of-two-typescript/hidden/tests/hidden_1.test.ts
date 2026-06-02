import { test } from "node:test";
import assert from "node:assert/strict";
import { isPowerOfTwo } from "../solution.ts";

test("isPowerOfTwo(16) should be true", () => {
  assert.equal(isPowerOfTwo(16), true);
});
