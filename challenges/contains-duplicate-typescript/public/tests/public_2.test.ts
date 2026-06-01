import { test } from "node:test";
import assert from "node:assert/strict";
import { containsDuplicate } from "../solution.ts";

test("containsDuplicate([1, 2, 3, 4]) should be false", () => {
  assert.equal(containsDuplicate([1, 2, 3, 4]), false);
});
