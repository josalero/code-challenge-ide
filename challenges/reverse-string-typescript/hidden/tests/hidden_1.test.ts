import { test } from "node:test";
import assert from "node:assert/strict";
import { reverseString } from "../solution.ts";

test("reverseString("a") should be "a"", () => {
  assert.equal(reverseString("a"), "a");
});
