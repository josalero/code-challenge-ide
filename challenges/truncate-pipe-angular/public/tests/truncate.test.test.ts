import { describe, expect, it } from "vitest";
import { TruncatePipe } from "../solution";

describe("TruncatePipe", () => {
  it("truncates", () => {
    const pipe = new TruncatePipe();
    expect(pipe.transform("Hello World", 5)).toBe("Hello...");
  });

  it("short text", () => {
    const pipe = new TruncatePipe();
    expect(pipe.transform("Hi", 5)).toBe("Hi");
  });
});
