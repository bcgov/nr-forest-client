import { describe, it, expect, beforeEach } from "vitest";

import { VueWrapper, mount } from "@vue/test-utils";
import AutoCompleteInputComponent from "@/components/forms/AutoCompleteInputComponent.vue";
import { wrap } from "module";

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

  it('emits the "update:selected-value" event when an option from the list is clicked', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [],
        label: id,
      },
    });

    const options = wrapper.findAll(".autocomplete-items-cell");
    const firstOption = options[0];
    await firstOption.trigger("click");
    expect(wrapper.emitted("update:selected-value")).toBeTruthy();
    expect(wrapper.emitted("update:selected-value")![0][0]).toEqual(
      contents[0]
    );
  });

  describe("when an option was previously selected", () => {
    let wrapper: VueWrapper<any, any>;

    beforeEach(async () => {
      wrapper = mount(AutoCompleteInputComponent, {
        props: {
          id,
          modelValue: "",
          contents,
          validations: [],
          label: id,
        },
      });

      const options = wrapper.findAll(".autocomplete-items-cell");
      const firstOption = options[0];
      await firstOption.trigger("click");

      // Now an option is effectively selected
      expect(wrapper.emitted("update:selected-value")).toBeTruthy();
      expect(wrapper.emitted("update:selected-value")![0][0]).toEqual(
        contents[0]
      );
    });

    it('emits the "update:selected-value" with undefined when user types something in the input field', async () => {
      // adding a 'Z' character to the end of the original value so to trigger an update:model-value
      await wrapper.setProps({ modelValue: "TANGOZ" });

      expect(wrapper.emitted("update:selected-value")).toHaveLength(2);
      expect(wrapper.emitted("update:selected-value")![1][0]).toEqual(
        undefined
      );
    });

    it('emits the "update:selected-value" with the newly selected value', async () => {
      const options = wrapper.findAll(".autocomplete-items-cell");
      const secondOption = options[1];
      await secondOption.trigger("click");

      expect(wrapper.emitted("update:selected-value")).toHaveLength(2);
      expect(wrapper.emitted("update:selected-value")![1][0]).toEqual(
        contents[1]
      );
    });
  });
});
