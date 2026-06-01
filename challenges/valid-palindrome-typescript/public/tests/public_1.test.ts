import { test } from "node:test";
import assert from "node:assert/strict";
import { isPalindrome } from "../solution.ts";

test("isPalindrome("A man, a plan, a can…") should be true", () => {
  assert.equal(isPalindrome("A man, a plan, a canal: Panama"), true);
});
