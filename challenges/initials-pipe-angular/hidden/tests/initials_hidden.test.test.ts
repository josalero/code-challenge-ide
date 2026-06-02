import { describe, expect, it } from "vitest";
import { InitialsPipe } from "../solution";

describe("InitialsPipe hidden", () => {
  it("single name", () => {
    const pipe = new InitialsPipe();
    expect(pipe.transform("Ada")).toBe("AD");
  });
});
