import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import CounterPanel from "../solution.vue";

describe("CounterPanel hidden", () => {
  it("double click", async () => {
    const wrapper = mount(CounterPanel);
    const btn = wrapper.get("button");
    await btn.trigger("click");
    await btn.trigger("click");
    expect(wrapper.get("[aria-live=polite]").text()).toBe("2");
  });
});
