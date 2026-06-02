import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Accordion } from "../solution";

describe("Accordion hidden", () => {
  it("closes other panel", () => {
    const items = [
      { title: "A", content: "Alpha" },
      { title: "B", content: "Beta" },
    ];
    render(<Accordion items={items} />);
    fireEvent.click(screen.getByRole("button", { name: "A" }));
    fireEvent.click(screen.getByRole("button", { name: "B" }));
    expect(screen.queryByText("Alpha")).not.toBeVisible();
    expect(screen.getByText("Beta")).toBeVisible();
  });
});
