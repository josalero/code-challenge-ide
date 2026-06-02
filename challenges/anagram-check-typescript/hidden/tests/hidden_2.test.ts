import { test } from "node:test";
import assert from "node:assert/strict";
import { isAnagram } from "../solution.ts";

test('isAnagram("ab", "a") should be false', () => {
  assert.equal(isAnagram("ab", "a"), false);
});
