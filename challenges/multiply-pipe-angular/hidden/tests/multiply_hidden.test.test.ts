import { describe, expect, it } from "vitest";
import { MultiplyPipe } from "../solution";

describe("MultiplyPipe hidden", () => {
  it("zero factor", () => {
    const pipe = new MultiplyPipe();
    expect(pipe.transform(9, 0)).toBe(0);
  });
});
