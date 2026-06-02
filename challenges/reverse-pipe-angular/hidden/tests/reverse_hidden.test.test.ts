import { describe, expect, it } from "vitest";
import { ReversePipe } from "../solution";

describe("ReversePipe hidden", () => {
  it("palindrome", () => {
    const pipe = new ReversePipe();
    expect(pipe.transform("level")).toBe("level");
  });
});
