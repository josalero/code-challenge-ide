import { test } from "node:test";
import assert from "node:assert/strict";
import { linearSearch } from "../solution.ts";

test("linearSearch([2, 3, 4], 3) should return index 1", () => {
  assert.equal(linearSearch([2, 3, 4], 3), 1);
});
