import { test } from "node:test";
import assert from "node:assert/strict";
import { omit } from "../solution.ts";

test("no keys", () => {
  assert.deepEqual(omit({ a: 1 }, []), { a: 1 });
});
