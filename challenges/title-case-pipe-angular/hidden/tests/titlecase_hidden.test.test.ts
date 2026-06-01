import { describe, expect, it } from "vitest";
import { TitleCasePipe } from "../solution";

describe("TitleCasePipe hidden", () => {
  it("single word", () => {
    const pipe = new TitleCasePipe();
    expect(pipe.transform("angular")).toBe("Angular");
  });
});
