import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ColorBox } from "../solution";

describe("ColorBox hidden", () => {
  it("named color", () => {
    render(<ColorBox color="blue" />);
    expect(screen.getByRole("img")).toHaveAttribute("aria-label", "blue");
  });
});
