import { test } from "node:test";
import assert from "node:assert/strict";
import { unique } from "../solution.ts";

test("strings", () => {
  assert.deepEqual(unique(["a", "b", "a"]), ["a", "b"]);
});
