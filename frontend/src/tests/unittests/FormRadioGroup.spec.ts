import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";

import FormRadioGroup from "../../common/FormRadioGroup.vue";
import FormFieldTemplate from "../../common/FormFieldTemplate.vue";

describe("FormRadioGroup", () => {
  it("component defined", () => {
    const wrapper = mount(FormRadioGroup, {
      props: { options: [] },
    });
    expect(wrapper).toBeDefined();
  });

  it("renders props fieldProps successfully", () => {
    const wrapper = mount(FormRadioGroup, {
      props: {
        fieldProps: { label: "Test Form Radio Group Title" },
        options: [],
      },
    });
    expect(wrapper.findComponent(FormFieldTemplate).exists()).toBe(true);
    expect(wrapper.text()).toContain("Test Form Radio Group Title");
  });

  it("renders props options successfully", () => {
    const wrapper = mount(FormRadioGroup, {
      props: {
        options: [
          { code: "1", text: "Option 1" },
          { code: "2", text: "Option 2" },
          { code: "3", text: "Option 3" },
        ],
      },
    });
    expect(wrapper.findAll("label")).toHaveLength(3);
    expect(wrapper.findAll("input")).toHaveLength(3);
    expect(wrapper.findAll("label")[0].text()).toBe("Option 1");
    expect(wrapper.findAll("input")[0].element.value).toBe("1");
    expect(wrapper.findAll("label")[1].text()).toBe("Option 2");
    expect(wrapper.findAll("input")[1].element.value).toBe("2");
    expect(wrapper.findAll("label")[2].text()).toBe("Option 3");
    expect(wrapper.findAll("input")[2].element.value).toBe("3");
  });

  it("renders props value, emit function successfully", async () => {
    const wrapper = mount(FormRadioGroup, {
      props: {
        fieldProps: { id: "test-form-Radiogroup-id" },
        value: ["1"],
        options: [
          { code: "1", text: "Option 1" },
          { code: "2", text: "Option 2" },
          { code: "3", text: "Option 3" },
        ],
      },
    });

    const label = wrapper.findAll("label");
    expect(label[0].classes("active")).toBe(true);
    expect(label[1].classes("active")).toBe(false);
    expect(label[2].classes("active")).toBe(false);
  });

  it("renders props disabled successfully", async () => {
    const wrapper = mount(FormRadioGroup, {
      props: {
        disabled: true,
        options: [
          { code: "1", text: "Option 1" },
          { code: "2", text: "Option 2" },
          { code: "3", text: "Option 3" },
        ],
      },
    });

    const inputs = wrapper.findAll("input");
    expect(inputs[0].attributes().disabled).toBeDefined();
    expect(inputs[1].attributes().disabled).toBeDefined();
    expect(inputs[2].attributes().disabled).toBeDefined();
  });
});
