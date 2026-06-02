import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import TodoList from "../solution.vue";

describe("TodoList", () => {
  it("adds todo", async () => {
    const wrapper = mount(TodoList);
    await wrapper.get("[aria-label=todo]").setValue("Learn Vue");
    await wrapper.get("button").trigger("click");
    expect(wrapper.text()).toContain("Learn Vue");
  });
});
