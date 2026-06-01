import { describe, expect, it } from "vitest";
import { SlugifyService } from "../solution";

describe("SlugifyService", () => {
  it("slugifies", () => {
    const svc = new SlugifyService();
    expect(svc.slugify("Hello World!")).toBe("hello-world");
  });
});
