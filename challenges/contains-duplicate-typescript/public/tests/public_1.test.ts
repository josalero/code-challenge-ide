import { test } from "node:test";
import assert from "node:assert/strict";
import { containsDuplicate } from "../solution.ts";

test("containsDuplicate([1, 2, 3, 1]) should be true", () => {
  assert.equal(containsDuplicate([1, 2, 3, 1]), true);
});
