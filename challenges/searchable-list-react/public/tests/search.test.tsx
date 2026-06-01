import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { SearchableList } from "../solution";

describe("SearchableList", () => {
  it("filters items", () => {
    render(<SearchableList items={["Apple", "Banana", "Apricot"]} />);
    fireEvent.change(screen.getByLabelText("search"), { target: { value: "ap" } });
    expect(screen.getByText("Apple")).toBeInTheDocument();
    expect(screen.getByText("Apricot")).toBeInTheDocument();
    expect(screen.queryByText("Banana")).not.toBeInTheDocument();
  });
});
