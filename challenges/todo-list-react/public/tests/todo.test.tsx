import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { TodoList } from "../solution";

describe("TodoList", () => {
  it("adds a todo on Enter", () => {
    render(<TodoList />);
    const input = screen.getByLabelText("new todo");
    fireEvent.change(input, { target: { value: "Buy milk" } });
    fireEvent.keyDown(input, { key: "Enter" });
    expect(screen.getByText("Buy milk")).toBeInTheDocument();
  });

  it("toggles done class", () => {
    render(<TodoList />);
    const input = screen.getByLabelText("new todo");
    fireEvent.change(input, { target: { value: "Task" } });
    fireEvent.keyDown(input, { key: "Enter" });
    fireEvent.click(screen.getByText("Task"));
    expect(screen.getByText("Task").closest("li")).toHaveClass("done");
  });
});
