import { test } from "node:test";
import assert from "node:assert/strict";
import { maxProfit } from "../solution.ts";

test("maxProfit([1, 2]) should equal 1", () => {
  assert.equal(maxProfit([1, 2]), 1);
});
