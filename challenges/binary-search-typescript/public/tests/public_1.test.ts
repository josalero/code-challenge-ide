import { test } from "node:test";
import assert from "node:assert/strict";
import { binarySearch } from "../solution.ts";

test("binarySearch([1, 3, 5, 7, 9], 3) should return index 1", () => {
  assert.equal(binarySearch([1, 3, 5, 7, 9], 3), 1);
});
