import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";

import FormCheckboxGroup from "../../common/FormCheckboxGroup.vue";
import FormFieldTemplate from "../../common/FormFieldTemplate.vue";

describe("FormCheckboxGroup", () => {
  it("component defined", () => {
    const wrapper = mount(FormCheckboxGroup);
    expect(wrapper).toBeDefined();
  });

  it("renders props fieldProps successfully", () => {
    const wrapper = mount(FormCheckboxGroup, {
      props: { fieldProps: { label: "Test Form Checkbox Group Title" } },
    });
    expect(wrapper.findComponent(FormFieldTemplate).exists()).toBe(true);
    expect(wrapper.text()).toContain("Test Form Checkbox Group Title");
  });

  it("renders props options successfully", () => {
    const wrapper = mount(FormCheckboxGroup, {
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
    const wrapper = mount(FormCheckboxGroup, {
      props: {
        fieldProps: { id: "test-form-checkboxgroup-id" },
        value: ["1"],
        options: [
          { code: "1", text: "Option 1" },
          { code: "2", text: "Option 2" },
        ],
      },
    });

    const label = wrapper.findAll("label");
    expect(label[0].classes("active")).toBe(true);
    expect(label[1].classes("active")).toBe(false);

    // todo: check how to setValue for the checkbox
    // await wrapper.find(".form-check").setValue(["1", "2"]);
    // expect(label[1].classes("active")).toBe(true);
    // // assert the emitted event has been performed
    // expect(wrapper.emitted()).toHaveProperty("updateValue");

    // const updateValueEvent = wrapper.emitted("updateValue");
    // // updateValue has been called once when doing the input.setValue
    // expect(updateValueEvent).toHaveLength(1);
    // // test the given parameters
    // expect(updateValueEvent[0]).toEqual([
    //   "test-form-checkboxgroup-id",
    //   ["1", "2"],
    // ]);
  });

  it("renders props disabled successfully", async () => {
    const wrapper = mount(FormCheckboxGroup, {
      props: {
        disabled: true,
        options: [
          { code: "1", text: "Option 1" },
          { code: "2", text: "Option 2" },
        ],
      },
    });

    const inputs = wrapper.findAll("input");
    expect(inputs[0].attributes().disabled).toBeDefined();
    expect(inputs[1].attributes().disabled).toBeDefined();
  });
});
