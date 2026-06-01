import { test } from "node:test";
import assert from "node:assert/strict";
import { plusOne } from "../solution.ts";

test("plusOne([9]) should return [1, 0]", () => {
  assert.deepEqual(plusOne([9]), [1, 0]);
});
