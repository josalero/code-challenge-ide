import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ThemeToggle } from "../solution";

describe("ThemeToggle hidden", () => {
  it("toggles back to light", () => {
    document.documentElement.setAttribute("data-theme", "dark");
    render(<ThemeToggle />);
    fireEvent.click(screen.getByRole("button"));
    expect(document.documentElement.getAttribute("data-theme")).toBe("light");
  });
});
