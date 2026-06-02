import { test } from "node:test";
import assert from "node:assert/strict";
import { maxProfit } from "../solution.ts";

test("maxProfit([7, 6, 4, 3, 1]) should equal 0", () => {
  assert.equal(maxProfit([7, 6, 4, 3, 1]), 0);
});
