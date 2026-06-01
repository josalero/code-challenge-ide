import { test } from "node:test";
import assert from "node:assert/strict";
import { missingNumber } from "../solution.ts";

test("missingNumber([3, 0, 1]) should equal 2", () => {
  assert.equal(missingNumber([3, 0, 1]), 2);
});
