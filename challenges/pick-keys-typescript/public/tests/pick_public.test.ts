import { test } from "node:test";
import assert from "node:assert/strict";
import { pick } from "../solution.ts";

test("picks keys", () => {
  assert.deepEqual(pick({ a: 1, b: 2, c: 3 }, ["a", "c"]), { a: 1, c: 3 });
});
