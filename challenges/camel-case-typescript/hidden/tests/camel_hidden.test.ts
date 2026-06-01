import { test } from "node:test";
import assert from "node:assert/strict";
import { toCamelCase } from "../solution.ts";

test("single segment", () => {
  assert.equal(toCamelCase("item"), "item");
});
