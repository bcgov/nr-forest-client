import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";

import MainHeader from "../common/MainHeader.vue";

describe("MainHeader", () => {
  it("renders properly", () => {
    const wrapper = mount(MainHeader);
    expect(wrapper.text()).toContain("FSA - Forest Client");
  });
});
