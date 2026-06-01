import { test } from "node:test";
import assert from "node:assert/strict";
import { maxProfit } from "../solution.ts";

test("maxProfit([7, 1, 5, 3, 6, 4]) should equal 5", () => {
  assert.equal(maxProfit([7, 1, 5, 3, 6, 4]), 5);
});
