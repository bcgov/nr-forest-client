import { describe, it, expect } from 'vitest';

import { mount } from "@vue/test-utils";
import TextInputComponent from "@/components/forms/TextInputComponent.vue";

describe("Text Input Component", () => {

  const id = "my-input";
  const validations = [(value: any) => (value ? "" : "Field is required")];

  it("renders the input field with the provided id", () => {
    const wrapper = mount(TextInputComponent, {
      props: {
        id,
        modelValue: "",
        validations: [],
      },
    });

    expect(wrapper.find(`#${id}`).exists()).toBe(true);
  });

  it('emits the "update" event with the updated value', async () => {

    const wrapper = mount(TextInputComponent, {
      props: {
        id,
        modelValue: "",
        validations: [],
      },
    });

    wrapper.getComponent(`#${id}`).setValue("John Doe");

    await wrapper.find(`#${id}`).trigger("blur");
    await wrapper.find(`#${id}`).trigger("input");

    expect(wrapper.emitted("update:modelValue")).toBeTruthy();
    expect(wrapper.emitted("update:modelValue")![0][0]).toEqual("John Doe");
  });

  it('emits the "error" event when there is a validation error', async () => {

    const wrapper = mount(TextInputComponent, {
      props: {
        id,
        modelValue: "",
        validations,
      },
    });

    await wrapper.find(`#${id}`).trigger("input");
    await wrapper.find(`#${id}`).trigger("blur");

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")![0][0]).toBe("Field is required");
  });



  it('emits the "empty" event when the input field is empty', async () => {
    const wrapper = mount(TextInputComponent, {
      props: {
        id,
        modelValue: "",
        validations: [],
      },
    });

    await wrapper.find(`#${id}`).trigger("input");
    await wrapper.find(`#${id}`).trigger("blur");

    expect(wrapper.emitted("empty")).toBeTruthy();
    expect(wrapper.emitted("empty")![0][0]).toBe(true);
  });
});
