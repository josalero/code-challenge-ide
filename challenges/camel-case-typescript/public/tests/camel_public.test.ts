import { test } from "node:test";
import assert from "node:assert/strict";
import { toCamelCase } from "../solution.ts";

test("kebab to camel", () => {
  assert.equal(toCamelCase("foo-bar-baz"), "fooBarBaz");
});

test("snake to camel", () => {
  assert.equal(toCamelCase("hello_world"), "helloWorld");
});
