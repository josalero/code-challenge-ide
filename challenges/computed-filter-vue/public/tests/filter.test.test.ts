import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import ItemFilter from "../solution.vue";

describe("ItemFilter", () => {
  it("filters", async () => {
    const wrapper = mount(ItemFilter, { props: { items: ["Cat", "Dog", "Caterpillar"] } });
    await wrapper.get("[aria-label=filter]").setValue("cat");
    expect(wrapper.text()).toContain("Cat");
    expect(wrapper.text()).toContain("Caterpillar");
    expect(wrapper.text()).not.toContain("Dog");
  });
});
