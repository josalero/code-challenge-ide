import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import TodoList from "../solution.vue";

describe("TodoList hidden", () => {
  it("adds two", async () => {
    const wrapper = mount(TodoList);
    await wrapper.get("[aria-label=todo]").setValue("A");
    await wrapper.get("button").trigger("click");
    await wrapper.get("[aria-label=todo]").setValue("B");
    await wrapper.get("button").trigger("click");
    expect(wrapper.findAll("li")).toHaveLength(2);
  });
});
