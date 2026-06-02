import { test } from "node:test";
import assert from "node:assert/strict";
import { linearSearch } from "../solution.ts";

test("hidden", () => {
  assert.equal(linearSearch([9], 9), 0);
  assert.equal(linearSearch([], 1), -1);
});
