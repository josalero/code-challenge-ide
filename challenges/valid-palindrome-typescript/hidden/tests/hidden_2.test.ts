import { test } from "node:test";
import assert from "node:assert/strict";
import { isPalindrome } from "../solution.ts";

test("isPalindrome(" ") should be true", () => {
  assert.equal(isPalindrome(" "), true);
});
