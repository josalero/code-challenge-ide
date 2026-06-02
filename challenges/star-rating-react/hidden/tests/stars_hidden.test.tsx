import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { StarRating } from "../solution";

describe("StarRating hidden", () => {
  it("respects max", () => {
    render(<StarRating max={3} />);
    expect(screen.getAllByRole("button")).toHaveLength(3);
  });
});
