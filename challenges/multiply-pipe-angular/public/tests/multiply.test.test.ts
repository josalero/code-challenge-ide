import { describe, expect, it } from "vitest";
import { MultiplyPipe } from "../solution";

describe("MultiplyPipe", () => {
  it("multiplies", () => {
    const pipe = new MultiplyPipe();
    expect(pipe.transform(3, 4)).toBe(12);
  });
});
