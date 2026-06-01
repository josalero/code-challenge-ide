import { test } from "node:test";
import assert from "node:assert/strict";
import { singleNumber } from "../solution.ts";

test("singleNumber([1]) should equal 1", () => {
  assert.equal(singleNumber([1]), 1);
});
