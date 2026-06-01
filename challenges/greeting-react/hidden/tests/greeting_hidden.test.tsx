import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Greeting } from "../solution";

describe("Greeting hidden", () => {
  it("handles another name", () => {
    render(<Greeting name="Grace" />);
    expect(screen.getByRole("heading")).toHaveTextContent("Hello, Grace!");
  });
});
