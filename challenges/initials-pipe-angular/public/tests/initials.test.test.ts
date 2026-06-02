import { describe, expect, it } from "vitest";
import { InitialsPipe } from "../solution";

describe("InitialsPipe", () => {
  it("two names", () => {
    const pipe = new InitialsPipe();
    expect(pipe.transform("John Doe")).toBe("JD");
  });
});
