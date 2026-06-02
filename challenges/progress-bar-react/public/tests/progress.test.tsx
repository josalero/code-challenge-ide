import { render } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ProgressBar } from "../solution";

describe("ProgressBar", () => {
  it("sets width from percent", () => {
    const { container } = render(<ProgressBar percent={40} />);
    const fill = container.querySelector(".fill") as HTMLElement;
    expect(fill.style.width).toBe("40%");
  });
});
