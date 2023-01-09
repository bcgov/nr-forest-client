import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";

import FormSelect from "../../common/FormSelect.vue";
import FormFieldTemplate from "../../common/FormFieldTemplate.vue";

import type { FormFieldTemplateType } from "../../core/AppType";

describe("FormSelect", () => {
  it("component defined", () => {
    const wrapper = mount(FormSelect, {
      props: { value: [], options: [] },
    });
    expect(wrapper).toBeDefined();
    expect(wrapper.find("select").exists()).toBe(true);
  });

  it("renders props fieldProps successfully", () => {
    const wrapper = mount(FormSelect, {
      props: {
        fieldProps: {
          label: "Test Form Select Title",
        } as FormFieldTemplateType,
        value: [],
        options: [],
      },
    });
    expect(wrapper.findComponent(FormFieldTemplate).exists()).toBe(true);
    expect(wrapper.text()).toContain("Test Form Select Title");
  });

  it("renders props options successfully", () => {
    const wrapper = mount(FormSelect, {
      props: {
        value: [],
        options: [
          { value: "1", text: "a" },
          { value: "2", text: "b" },
        ],
      },
    });
    expect(wrapper.findAll("option")).toHaveLength(2);
    expect(wrapper.findAll("option")[0].text()).toBe("a");
    expect(wrapper.findAll("option")[0].element.value).toBe("1");
    expect(wrapper.findAll("option")[1].text()).toBe("b");
    expect(wrapper.findAll("option")[1].element.value).toBe("2");
  });

  it("renders props value, emit function successfully", async () => {
    const wrapper = mount(FormSelect, {
      props: {
        fieldProps: { id: "test-form-select-id" },
        value: "1",
        options: [
          { value: "1", text: "a" },
          { value: "2", text: "b" },
        ],
      },
    });

    const select = wrapper.find("select");
    expect(select.element.value).toBe("1");
    await select.setValue("2");
    expect(select.element.value).toBe("2");
    // assert the emitted event has been performed
    expect(wrapper.emitted()).toHaveProperty("updateValue");

    let updateValueEvent = wrapper.emitted("updateValue");
    // updateValue has been called once when doing the input.setValue
    expect(updateValueEvent).toHaveLength(1);
    // test the given parameters
    updateValueEvent = updateValueEvent || [];
    expect(updateValueEvent[0]).toEqual(["test-form-select-id", "2"]);
  });

  it("renders props disabled successfully", async () => {
    const wrapper = mount(FormSelect, {
      props: { disabled: true, value: [], options: [] },
    });

    const select = wrapper.find("select");
    expect(select.attributes().disabled).toBeDefined();
  });
});
