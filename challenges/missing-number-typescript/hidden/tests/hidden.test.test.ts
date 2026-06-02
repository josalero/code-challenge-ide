import { test } from "node:test";
import assert from "node:assert/strict";
import { missingNumber } from "../solution.ts";

test("hidden", () => {
  assert.equal(missingNumber([9, 6, 4, 2, 3, 5, 7, 0, 1]), 8);
  assert.equal(missingNumber([1]), 0);
});
