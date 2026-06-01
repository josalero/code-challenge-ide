import { test } from "node:test";
import assert from "node:assert/strict";
import { unique } from "../solution.ts";

test("removes dupes", () => {
  assert.deepEqual(unique([1, 2, 2, 3, 1]), [1, 2, 3]);
});
