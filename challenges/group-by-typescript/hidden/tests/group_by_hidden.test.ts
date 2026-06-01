import { test } from "node:test";
import assert from "node:assert/strict";
import { groupBy } from "../solution.ts";

test("empty", () => {
  assert.deepEqual(groupBy([], (x) => String(x)), {});
});
