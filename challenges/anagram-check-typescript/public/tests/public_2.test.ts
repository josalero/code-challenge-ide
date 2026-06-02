import { test } from "node:test";
import assert from "node:assert/strict";
import { isAnagram } from "../solution.ts";

test('isAnagram("rat", "car") should be false', () => {
  assert.equal(isAnagram("rat", "car"), false);
});
