import { test } from "node:test";
import assert from "node:assert/strict";
import { mergeSorted } from "../solution.ts";

test("mergeSorted([1, 1], [1]) should return [1, 1, 1]", () => {
  assert.deepEqual(mergeSorted([1, 1], [1]), [1, 1, 1]);
});
