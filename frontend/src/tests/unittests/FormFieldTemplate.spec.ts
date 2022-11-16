import { describe, it, expect } from "vitest";
import { mount, shallowMount } from "@vue/test-utils";

import FormFieldTemplate from "../../common/FormFieldTemplate.vue";
import FormFieldTitle from "../../common/FormFieldTitle.vue";

describe("FormFieldTemplate", () => {
  it("component defined", () => {
    const wrapper = shallowMount(FormFieldTemplate);
    expect(wrapper).toBeDefined();
  });

  it("renders with no properties successfully", () => {
    const wrapper = mount(FormFieldTemplate);
    expect(wrapper.text()).toEqual("");
    expect(wrapper.findComponent(FormFieldTitle).exists()).toBe(false);
    expect(wrapper.find(".form-field-note").exists()).toBe(false);
  });

  it("renders props label successfully", () => {
    // only render FormFieldTitle component when label is not null
    const wrapper = mount(FormFieldTemplate, {
      props: {
        fieldProps: { label: "Test Form Field Template Title" },
      },
    });
    expect(wrapper.findComponent(FormFieldTitle).exists()).toBe(true);
    expect(wrapper.text()).toContain("Test Form Field Template Title");
  });

  it("renders props required successfully", () => {
    const wrapper = mount(FormFieldTemplate, {
      props: {
        fieldProps: { label: "Test Form Field Template Title", required: true },
      },
    });
    expect(wrapper.find("svg").exists()).toBe(true);
  });

  it("renders props tooltip successfully", async () => {
    const wrapper = mount(FormFieldTemplate, {
      props: {
        fieldProps: {
          label: "Test Form Field Template Title",
          tooltip: "Test Tooltip",
        },
      },
    });
    expect(wrapper.text()).not.toContain("Test Tooltip");
    expect(wrapper.find("svg").exists()).toBe(true);

    // todo: test hover over tooltip
    // await wrapper.find("svg").trigger("mouseover");
    // expect(wrapper.text()).toContain("Test Tooltip");
    // expect(wrapper.find(".popover.show").exists()).toBe(true);
  });

  it("renders props note successfully", async () => {
    const wrapper = shallowMount(FormFieldTemplate, {
      props: {
        fieldProps: { note: "Test Tooltip" },
      },
    });
    expect(wrapper.find(".form-field-note").exists()).toBe(true);
    expect(wrapper.text()).toContain("Test Tooltip");
  });
});
