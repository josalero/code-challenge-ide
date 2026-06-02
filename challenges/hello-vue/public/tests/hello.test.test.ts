import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import Hello from "../solution.vue";

describe("Hello", () => {
  it("greets", () => {
    const wrapper = mount(Hello, { props: { name: "Vue" } });
    expect(wrapper.get("h1").text()).toBe("Hello, Vue!");
  });
});
