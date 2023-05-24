import { describe, it, expect } from 'vitest';

import { mount, shallowMount } from "@vue/test-utils";
import AutoCompleteInputComponent from "@/components/forms/AutoCompleteInputComponent.vue";

describe("Text Input Component", () => {

  const id = "my-input";
  const validations = [(value: any) => (value ? "" : "Field is required")];
  const dataList = [
    { code: "TA", value: "TANGO" },
    { code: "TB", value: "TAMBOR" },
    { code: "TC", value: "TAMCADA" },
    { code: "TD", value: "TADANARA" },
  ];

  it("renders the input field with the provided id", () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        dataList,
        validations: [],
      },
    });

    expect(wrapper.find(`#${id}`).exists()).toBe(true);
    expect(wrapper.find('datalist').exists()).toBe(true);

    const options = wrapper.findAll('option');

    expect(options.length).toBe(dataList.length);
    for (let index = 0; index < dataList.length; index++) {
      expect(options[index].attributes('value')).toBe(dataList[index].value);
    }

  });

  it('emits the "update" event with the updated value', async () => {

    const wrapper = shallowMount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        dataList,
        validations: [],
      },
    });

    wrapper.find(`#${id}`).setValue("TANGO");

    await wrapper.find(`#${id}`).trigger("blur");
    await wrapper.find(`#${id}`).trigger("input");

    expect(wrapper.emitted("update:modelValue")).toBeTruthy();
    expect(wrapper.emitted("update:modelValue")![0][0]).toEqual("TANGO");

    expect(wrapper.emitted("update:selectedValue")).toBeTruthy();
    expect(wrapper.emitted("update:selectedValue")![0][0]).toStrictEqual(dataList[0]);
  });

  it('emits the "error" event when there is a validation error', async () => {

    const wrapper = shallowMount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        dataList,
        validations,
      },
    });

    await wrapper.find(`#${id}`).trigger("input");
    await wrapper.find(`#${id}`).trigger("blur");

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")![0][0]).toBe("Field is required");
  });

  it('emits the "empty" event when the input field is empty', async () => {
    const wrapper = shallowMount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        dataList,
        validations: [],
      },
    });

    await wrapper.find(`#${id}`).trigger("input");
    await wrapper.find(`#${id}`).trigger("blur");

    expect(wrapper.emitted("empty")).toBeTruthy();
    expect(wrapper.emitted("empty")![0][0]).toBe(true);
  });

});
