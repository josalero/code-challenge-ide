import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Counter } from "../solution";

describe("Counter", () => {
  it("shows initial count", () => {
    render(<Counter initial={3} />);
    expect(screen.getByRole("button")).toHaveTextContent("3");
  });

  it("increments on click", () => {
    render(<Counter initial={0} />);
    fireEvent.click(screen.getByRole("button"));
    expect(screen.getByRole("button")).toHaveTextContent("1");
  });
});
