import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import Counter from "../solution.vue";

describe("Counter", () => {
  it("shows initial", () => {
    const wrapper = mount(Counter, { props: { initial: 4 } });
    expect(wrapper.get("button").text()).toBe("4");
  });

  it("increments", async () => {
    const wrapper = mount(Counter, { props: { initial: 0 } });
    await wrapper.get("button").trigger("click");
    expect(wrapper.get("button").text()).toBe("1");
  });
});
