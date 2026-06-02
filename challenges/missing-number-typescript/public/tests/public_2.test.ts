import { test } from "node:test";
import assert from "node:assert/strict";
import { missingNumber } from "../solution.ts";

test("missingNumber([0]) should equal 1", () => {
  assert.equal(missingNumber([0]), 1);
});
