import { test } from "node:test";
import assert from "node:assert/strict";
import { chunk } from "../solution.ts";

test("empty input", () => {
  assert.deepEqual(chunk([], 2), []);
});
