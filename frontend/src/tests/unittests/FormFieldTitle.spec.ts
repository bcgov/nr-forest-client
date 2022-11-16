import { describe, it, expect } from "vitest";
import { mount, shallowMount } from "@vue/test-utils";

import FormFieldTitle from "../../common/FormFieldTitle.vue";

describe("FormFieldTitle", () => {
  it("component defined", () => {
    const wrapper = shallowMount(FormFieldTitle);
    expect(wrapper).toBeDefined();
  });

  it("renders with no properties successfully", () => {
    const wrapper = shallowMount(FormFieldTitle);

    expect(wrapper.text()).toEqual("");
    expect(wrapper.find("svg").exists()).toBe(false);
  });

  it("renders props label successfully", () => {
    const wrapper = shallowMount(FormFieldTitle, {
      props: {
        label: "Test Form Field Title",
      },
    });
    expect(wrapper.props().label).toBe("Test Form Field Title");
    expect(wrapper.text()).toContain("Test Form Field Title");
  });

  it("renders props required successfully", () => {
    const wrapper = mount(FormFieldTitle, {
      props: {
        required: true,
      },
    });
    expect(wrapper.props().required).toBe(true);
    expect(wrapper.find("svg").exists()).toBe(true);
  });

  it("renders props tooltip successfully", async () => {
    const wrapper = mount(FormFieldTitle, {
      props: {
        tooltip: "Test Tooltip",
      },
    });
    expect(wrapper.props().tooltip).toBe("Test Tooltip");
    expect(wrapper.text()).toEqual("");
    expect(wrapper.find("svg").exists()).toBe(true);

    // todo: test hover over tooltip
    // await wrapper.find("svg").trigger("mouseover");
    // expect(wrapper.text()).toContain("Test Tooltip");
    // expect(wrapper.find(".popover.show").exists()).toBe(true);
  });
});
