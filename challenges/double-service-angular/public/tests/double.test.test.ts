import { describe, expect, it } from "vitest";
import { DoubleService } from "../solution";

describe("DoubleService", () => {
  it("doubles", () => {
    const svc = new DoubleService();
    expect(svc.double(3)).toBe(6);
  });

  it("zero", () => {
    const svc = new DoubleService();
    expect(svc.double(0)).toBe(0);
  });
});
