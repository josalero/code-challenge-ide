import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import Hello from "../solution.vue";

describe("Hello hidden", () => {
  it("another name", () => {
    const wrapper = mount(Hello, { props: { name: "World" } });
    expect(wrapper.get("h1").text()).toBe("Hello, World!");
  });
});
