import { test } from "node:test";
import assert from "node:assert/strict";
import { capitalizeWords } from "../solution.ts";

test("capitalizes each word", () => {
  assert.equal(capitalizeWords("hello world"), "Hello World");
});

test("single word", () => {
  assert.equal(capitalizeWords("ada"), "Ada");
});
