import { test } from "node:test";
import assert from "node:assert/strict";
import { missingNumber } from "../solution.ts";

test("missingNumber([1]) should equal 0", () => {
  assert.equal(missingNumber([1]), 0);
});
