import { test } from "node:test";
import assert from "node:assert/strict";
import { isPalindrome } from "../solution.ts";

test("isPalindrome("race a car") should be false", () => {
  assert.equal(isPalindrome("race a car"), false);
});
