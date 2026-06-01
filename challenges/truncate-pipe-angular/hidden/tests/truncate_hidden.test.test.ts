import { describe, expect, it } from "vitest";
import { TruncatePipe } from "../solution";

describe("TruncatePipe hidden", () => {
  it("exact limit", () => {
    const pipe = new TruncatePipe();
    expect(pipe.transform("abcde", 5)).toBe("abcde");
  });
});
