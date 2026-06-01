import { test } from "node:test";
import assert from "node:assert/strict";
import { reverseString } from "../solution.ts";

test("reverseString("hello") should be "olleh"", () => {
  assert.equal(reverseString("hello"), "olleh");
});
