import { test } from "node:test";
import assert from "node:assert/strict";
import { pick } from "../solution.ts";

test("empty keys", () => {
  assert.deepEqual(pick({ a: 1 }, []), {});
});
