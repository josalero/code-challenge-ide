import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import CounterPanel from "../solution.vue";

describe("CounterPanel", () => {
  it("increments", async () => {
    const wrapper = mount(CounterPanel);
    await wrapper.get("button").trigger("click");
    expect(wrapper.get("[aria-live=polite]").text()).toBe("1");
  });
});
