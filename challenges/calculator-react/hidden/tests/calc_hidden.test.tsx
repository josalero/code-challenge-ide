import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Calculator } from "../solution";

describe("Calculator hidden", () => {
  it("negative numbers", () => {
    render(<Calculator />);
    fireEvent.change(screen.getByLabelText("a"), { target: { value: "-1" } });
    fireEvent.change(screen.getByLabelText("b"), { target: { value: "1" } });
    expect(screen.getByRole("status")).toHaveTextContent("0");
  });
});
