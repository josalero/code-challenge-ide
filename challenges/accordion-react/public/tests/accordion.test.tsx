import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Accordion } from "../solution";

describe("Accordion", () => {
  const items = [
    { title: "One", content: "First" },
    { title: "Two", content: "Second" },
  ];

  it("shows content when header clicked", () => {
    render(<Accordion items={items} />);
    fireEvent.click(screen.getByRole("button", { name: "One" }));
    expect(screen.getByText("First")).toBeVisible();
  });
});
