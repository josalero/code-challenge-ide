import { test } from "node:test";
import assert from "node:assert/strict";
import { truncate } from "../solution.ts";

test("custom suffix", () => {
  assert.equal(truncate("abcdef", 5, "…"), "abcd…");
});
