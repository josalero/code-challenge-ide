import { test } from "node:test";
import assert from "node:assert/strict";
import { groupBy } from "../solution.ts";

test("groups by parity", () => {
  assert.deepEqual(
    groupBy([1, 2, 3, 4], (n) => (n % 2 === 0 ? "even" : "odd")),
    { odd: [1, 3], even: [2, 4] }
  );
});
