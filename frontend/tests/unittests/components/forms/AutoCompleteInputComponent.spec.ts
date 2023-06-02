import { describe, it, expect } from "vitest";

import { mount } from "@vue/test-utils";
import AutoCompleteInputComponent from "@/components/forms/AutoCompleteInputComponent.vue";
import { wrap } from "module";

//TODO: Implement later the click functionality

describe("Auto Complete Input Component", () => {
  const id = "my-input";
  const validations = [(value: any) => (value ? "" : "Field is required")];
  const contents = [
    { code: "TA", name: "TANGO" },
    { code: "TB", name: "TAMBOR" },
    { code: "TC", name: "TAMCADA" },
    { code: "TD", name: "TADANARA" },
  ];

  it("renders the input field with the provided id", () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [],
        label: id,
      },
    });

    expect(wrapper.find(`#${id}`).exists()).toBe(true);
    expect(wrapper.find(`#${id}list`).exists()).toBe(true);

    const options = wrapper.findAll(".autocomplete-items-cell");

    expect(options.length).toBe(contents.length);
    for (let index = 0; index < contents.length; index++) {
      expect(options[index].text()).toContain(contents[index].name);
    }
  });

  it('emits the "update" event with the updated value', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [],
        label: id,
        tip: "",
      },
    });

    await wrapper.setProps({ modelValue: "TANGO" });
    await wrapper.find(`#${id}`).trigger("blur");
    await wrapper.find(`#${id}`).trigger("input");

    expect(wrapper.emitted("update:model-value")).toBeTruthy();
    expect(wrapper.emitted("update:model-value")![0][0]).toEqual("TANGO");
  });

  it('emits the "error" event when there is a validation error', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations,
        label: id,
      },
    });

    await wrapper.find(`#${id}`).trigger("input");
    await wrapper.find(`#${id}`).trigger("blur");

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")![0][0]).toBe("Field is required");
  });

  it('emits the "empty" event when the input field is empty', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [],
        label: id,
      },
    });

    await wrapper.find(`#${id}`).trigger("input");
    await wrapper.find(`#${id}`).trigger("blur");

    expect(wrapper.emitted("empty")).toBeTruthy();
    expect(wrapper.emitted("empty")![0][0]).toBe(true);
  });
});
