import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import ItemFilter from "../solution.vue";

describe("ItemFilter hidden", () => {
  it("shows all when empty", () => {
    const wrapper = mount(ItemFilter, { props: { items: ["A", "B"] } });
    expect(wrapper.text()).toContain("A");
    expect(wrapper.text()).toContain("B");
  });
});
