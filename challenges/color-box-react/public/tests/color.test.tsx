import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ColorBox } from "../solution";

describe("ColorBox", () => {
  it("applies background", () => {
    render(<ColorBox color="#ff0000" />);
    const box = screen.getByRole("img");
    expect(box).toHaveStyle({ backgroundColor: "rgb(255, 0, 0)" });
  });
});
