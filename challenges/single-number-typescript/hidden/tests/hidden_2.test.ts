import { test } from "node:test";
import assert from "node:assert/strict";
import { singleNumber } from "../solution.ts";

test("singleNumber([6, 3, 6]) should equal 3", () => {
  assert.equal(singleNumber([6, 3, 6]), 3);
});
