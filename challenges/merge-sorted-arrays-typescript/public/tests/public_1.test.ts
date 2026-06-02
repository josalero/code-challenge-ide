import { test } from "node:test";
import assert from "node:assert/strict";
import { mergeSorted } from "../solution.ts";

test("mergeSorted([1, 2, 3], [2, 5, 6]) should return [1, 2, 2, 3, 5, 6]", () => {
  assert.deepEqual(mergeSorted([1, 2, 3], [2, 5, 6]), [1, 2, 2, 3, 5, 6]);
});
