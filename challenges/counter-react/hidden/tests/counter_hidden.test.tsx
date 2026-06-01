import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Counter } from "../solution";

describe("Counter hidden", () => {
  it("increments twice from 5", () => {
    render(<Counter initial={5} />);
    const btn = screen.getByRole("button");
    fireEvent.click(btn);
    fireEvent.click(btn);
    expect(btn).toHaveTextContent("7");
  });
});
