import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import Counter from "../solution.vue";

describe("Counter hidden", () => {
  it("double increment", async () => {
    const wrapper = mount(Counter, { props: { initial: 2 } });
    const btn = wrapper.get("button");
    await btn.trigger("click");
    await btn.trigger("click");
    expect(btn.text()).toBe("4");
  });
});
