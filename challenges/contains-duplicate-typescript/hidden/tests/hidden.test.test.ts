import { test } from "node:test";
import assert from "node:assert/strict";
import { containsDuplicate } from "../solution.ts";

test("hidden", () => {
  assert.equal(containsDuplicate([1, 1]), true);
  assert.equal(containsDuplicate([]), false);
});
