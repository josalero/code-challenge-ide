import { test } from "node:test";
import assert from "node:assert/strict";
import { linearSearch } from "../solution.ts";

test("linearSearch([9], 9) should return index 0", () => {
  assert.equal(linearSearch([9], 9), 0);
});
