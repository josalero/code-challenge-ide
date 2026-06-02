import { test } from "node:test";
import assert from "node:assert/strict";
import { containsDuplicate } from "../solution.ts";

test("containsDuplicate([]) should be false", () => {
  assert.equal(containsDuplicate([]), false);
});
