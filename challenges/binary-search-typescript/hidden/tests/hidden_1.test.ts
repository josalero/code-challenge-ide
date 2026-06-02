import { test } from "node:test";
import assert from "node:assert/strict";
import { binarySearch } from "../solution.ts";

test("binarySearch([2, 4, 6], 2) should return index 0", () => {
  assert.equal(binarySearch([2, 4, 6], 2), 0);
});
