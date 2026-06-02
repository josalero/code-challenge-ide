import { test } from "node:test";
import assert from "node:assert/strict";
import { truncate } from "../solution.ts";

test("truncates long text", () => {
  assert.equal(truncate("Hello World", 8), "Hello...");
});

test("short text unchanged", () => {
  assert.equal(truncate("Hi", 5), "Hi");
});
