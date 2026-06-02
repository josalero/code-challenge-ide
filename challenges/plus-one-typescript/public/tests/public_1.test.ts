import { test } from "node:test";
import assert from "node:assert/strict";
import { plusOne } from "../solution.ts";

test("plusOne([1, 2, 3]) should return [1, 2, 4]", () => {
  assert.deepEqual(plusOne([1, 2, 3]), [1, 2, 4]);
});
