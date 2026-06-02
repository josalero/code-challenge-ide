import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Greeting } from "../solution";

describe("Greeting", () => {
  it("renders name", () => {
    render(<Greeting name="Ada" />);
    expect(screen.getByRole("heading")).toHaveTextContent("Hello, Ada!");
  });
});
