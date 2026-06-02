import { test } from "node:test";
import assert from "node:assert/strict";
import { maxProfit } from "../solution.ts";

test("maxProfit([2, 4, 1]) should equal 2", () => {
  assert.equal(maxProfit([2, 4, 1]), 2);
});
