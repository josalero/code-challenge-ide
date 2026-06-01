import { test } from "node:test";
import assert from "node:assert/strict";
import { linearSearch } from "../solution.ts";

test("public", () => {
  assert.equal(linearSearch([2, 3, 4], 3), 1);
  assert.equal(linearSearch([1, 2], 5), -1);
});
