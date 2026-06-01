import { test } from "node:test";
import assert from "node:assert/strict";
import { omit } from "../solution.ts";

test("omits single key", () => {
  assert.deepEqual(omit({ a: 1, b: 2 }, ["b"]), { a: 1 });
});

test("omits multiple", () => {
  assert.deepEqual(omit({ x: 1, y: 2, z: 3 }, ["y", "z"]), { x: 1 });
});
