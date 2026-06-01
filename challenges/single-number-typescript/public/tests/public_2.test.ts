import { test } from "node:test";
import assert from "node:assert/strict";
import { singleNumber } from "../solution.ts";

test("singleNumber([4, 1, 2, 1, 2]) should equal 4", () => {
  assert.equal(singleNumber([4, 1, 2, 1, 2]), 4);
});
