import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ThemeToggle } from "../solution";

describe("ThemeToggle", () => {
  it("toggles data-theme", () => {
    document.documentElement.setAttribute("data-theme", "light");
    render(<ThemeToggle />);
    fireEvent.click(screen.getByRole("button"));
    expect(document.documentElement.getAttribute("data-theme")).toBe("dark");
  });
});
