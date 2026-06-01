import { test } from "node:test";
import assert from "node:assert/strict";
import { isPowerOfTwo } from "../solution.ts";

test("public", () => {
  assert.equal(isPowerOfTwo(1), true);
  assert.equal(isPowerOfTwo(3), false);
});
