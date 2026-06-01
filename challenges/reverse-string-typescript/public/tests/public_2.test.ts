import { test } from "node:test";
import assert from "node:assert/strict";
import { reverseString } from "../solution.ts";

test("reverseString("") should be """, () => {
  assert.equal(reverseString(""), "");
});
