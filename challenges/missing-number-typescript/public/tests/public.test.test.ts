import { test } from "node:test";
import assert from "node:assert/strict";
import { missingNumber } from "../solution.ts";

test("public", () => {
  assert.equal(missingNumber([3, 0, 1]), 2);
  assert.equal(missingNumber([0]), 1);
});
