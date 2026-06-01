import { describe, expect, it } from "vitest";
import { DoubleService } from "../solution";

describe("DoubleService hidden", () => {
  it("negative", () => {
    const svc = new DoubleService();
    expect(svc.double(-4)).toBe(-8);
  });
});
