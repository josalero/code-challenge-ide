import { describe, expect, it } from "vitest";
import { TitleCasePipe } from "../solution";

describe("TitleCasePipe", () => {
  it("title cases", () => {
    const pipe = new TitleCasePipe();
    expect(pipe.transform("hello world")).toBe("Hello World");
  });
});
