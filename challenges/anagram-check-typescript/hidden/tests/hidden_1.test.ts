import { test } from "node:test";
import assert from "node:assert/strict";
import { isAnagram } from "../solution.ts";

test("isAnagram("a", "a") should be true", () => {
  assert.equal(isAnagram("a", "a"), true);
});
