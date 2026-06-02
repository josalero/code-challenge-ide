import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { SearchableList } from "../solution";

describe("SearchableList hidden", () => {
  it("empty query shows all", () => {
    render(<SearchableList items={["X", "Y"]} />);
    expect(screen.getByText("X")).toBeInTheDocument();
    expect(screen.getByText("Y")).toBeInTheDocument();
  });
});
