import { test } from "node:test";
import assert from "node:assert/strict";
import { isValidParentheses } from "../solution.ts";

test("isValid("(]") should be false", () => {
  assert.equal(isValidParentheses("(]"), false);
});
