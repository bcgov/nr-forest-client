import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";

import FormCheckbox from "../../common/FormCheckbox.vue";
import FormFieldTemplate from "../../common/FormFieldTemplate.vue";

import type { FormFieldTemplateType } from "../../core/FormType";

describe("FormCheckbox", () => {
  it("component defined", () => {
    const wrapper = mount(FormCheckbox, { props: { value: false } });
    expect(wrapper).toBeDefined();
  });

  it("renders props fieldProps successfully", () => {
    const wrapper = mount(FormCheckbox, {
      props: {
        fieldProps: {
          label: "Test Form Checkbox Title",
        } as FormFieldTemplateType,
        value: false,
      },
    });
    expect(wrapper.findComponent(FormFieldTemplate).exists()).toBe(true);
    expect(wrapper.text()).toContain("Test Form Checkbox Title");
  });

  it("renders props value, emit function successfully", async () => {
    const wrapper = mount(FormCheckbox, {
      props: {
        fieldProps: { id: "test-form-checkbox-id" },
        value: false,
      },
    });

    const label = wrapper.find("label");
    expect(label.classes("active")).toBe(false);

    await wrapper.setProps({ value: true });
    expect(label.classes("active")).toBe(true);

    // todo: check how to check/uncheck the checkbox
    // const input = wrapper.find('input[type="checkbox"]');
    // await input.setChecked();
    // expect(input.element.checked).toBeTruthy();

    // todo: test emit function
    // const updateValueEvent = wrapper.emitted("updateValue");
    // // updateValue has been called once when doing the input.setValue
    // expect(updateValueEvent).toHaveLength(1);
    // // test the given parameters
    // expect(updateValueEvent[0]).toEqual([
    //   "test-form-checkboxgroup-id",
    //   true,
    // ]);
  });

  it("renders props disabled successfully", async () => {
    const wrapper = mount(FormCheckbox, {
      props: {
        disabled: true,
        value: false,
      },
    });

    const input = wrapper.find("input");
    expect(input.attributes().disabled).toBeDefined();
  });
});
