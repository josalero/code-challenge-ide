import { test } from "node:test";
import assert from "node:assert/strict";
import { isAnagram } from "../solution.ts";

test('isAnagram("anagram", "nagaram") should be true', () => {
  assert.equal(isAnagram("anagram", "nagaram"), true);
});
