import { test } from "node:test";
import assert from "node:assert/strict";
import { flatten } from "../solution.ts";

test("empty", () => {
  assert.deepEqual(flatten([]), []);
});
