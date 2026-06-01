import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Calculator } from "../solution";

describe("Calculator", () => {
  it("sums inputs", () => {
    render(<Calculator />);
    fireEvent.change(screen.getByLabelText("a"), { target: { value: "2" } });
    fireEvent.change(screen.getByLabelText("b"), { target: { value: "3" } });
    expect(screen.getByRole("status")).toHaveTextContent("5");
  });
});
