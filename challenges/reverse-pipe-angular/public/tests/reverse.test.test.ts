import { describe, expect, it } from "vitest";
import { ReversePipe } from "../solution";

describe("ReversePipe", () => {
  it("reverses text", () => {
    const pipe = new ReversePipe();
    expect(pipe.transform("abc")).toBe("cba");
  });

  it("empty string", () => {
    const pipe = new ReversePipe();
    expect(pipe.transform("")).toBe("");
  });
});
