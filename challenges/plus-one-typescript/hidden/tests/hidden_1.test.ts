import { test } from "node:test";
import assert from "node:assert/strict";
import { plusOne } from "../solution.ts";

test("plusOne([0]) should return [1]", () => {
  assert.deepEqual(plusOne([0]), [1]);
});
