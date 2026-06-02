import { test } from "node:test";
import assert from "node:assert/strict";
import { linearSearch } from "../solution.ts";

test("linearSearch([1, 2], 5) should return index -1", () => {
  assert.equal(linearSearch([1, 2], 5), -1);
});
