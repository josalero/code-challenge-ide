import { describe, expect, it } from "vitest";
import { SlugifyService } from "../solution";

describe("SlugifyService hidden", () => {
  it("trims hyphens", () => {
    const svc = new SlugifyService();
    expect(svc.slugify("  A--B  ")).toBe("a-b");
  });
});
