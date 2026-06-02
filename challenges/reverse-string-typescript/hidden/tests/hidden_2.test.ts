import { test } from "node:test";
import assert from "node:assert/strict";
import { reverseString } from "../solution.ts";

test("reverseString("race") should be "ecar"", () => {
  assert.equal(reverseString("race"), "ecar");
});
