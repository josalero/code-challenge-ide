import { test } from "node:test";
import assert from "node:assert/strict";
import { isPowerOfTwo } from "../solution.ts";

test("isPowerOfTwo(1) should be true", () => {
  assert.equal(isPowerOfTwo(1), true);
});
