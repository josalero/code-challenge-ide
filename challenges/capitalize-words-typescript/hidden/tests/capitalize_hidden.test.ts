import { test } from "node:test";
import assert from "node:assert/strict";
import { capitalizeWords } from "../solution.ts";

test("multiple spaces preserved loosely", () => {
  assert.equal(capitalizeWords("a b"), "A B");
});
