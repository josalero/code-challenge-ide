import { render } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ProgressBar } from "../solution";

describe("ProgressBar hidden", () => {
  it("zero percent", () => {
    const { container } = render(<ProgressBar percent={0} />);
    expect((container.querySelector(".fill") as HTMLElement).style.width).toBe("0%");
  });
});
