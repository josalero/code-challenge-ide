import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { StarRating } from "../solution";

describe("StarRating", () => {
  it("selects third star", () => {
    render(<StarRating />);
    fireEvent.click(screen.getByLabelText("star 3"));
    expect(screen.getByLabelText("star 3")).toHaveAttribute("aria-pressed", "true");
  });
});
