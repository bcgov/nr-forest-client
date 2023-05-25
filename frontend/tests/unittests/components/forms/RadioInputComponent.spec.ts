import { describe, it, expect } from 'vitest';

import { mount } from "@vue/test-utils";
import RadioInputComponent from '@/components/forms/RadioInputComponent.vue';

describe("Radio Input Component", () => {

  const id = "my-input";
  const values = [
    { value: 'A', text: 'First' }, { value: 'B', text: 'Second' }
  ];
  const validations = [(value: any) => (value && value === 'A' ? "Can't select A" : "")];

  it("renders the input field with the provided id", () => {

    const wrapper = mount(RadioInputComponent, {
      props: {
        id,
        modelValue: values,
        validations: [],
      },
    });

    expect(wrapper.find(`#${id}_A`).exists()).toBe(true);
    expect(wrapper.find(`#${id}_B`).exists()).toBe(true);
  });

  it('emits the "error" event when there is a validation error', async () => {

    const wrapper = mount(RadioInputComponent, {
      props: {
        id,
        modelValue: values,
        validations,
      },
    });

    await wrapper.find('input[type=radio][value=A]').setValue();
    await wrapper.find('input[type=radio][value=A]').trigger('change');

    const event = wrapper.emitted("error");

    expect(event).toBeTruthy();
    expect(event).toHaveLength(1);
    expect(event[0][0]).toBe("Can't select A");
  });

  it('emits the "update" event when selected', async () => {

    const wrapper = mount(RadioInputComponent, {
      props: {
        id,
        modelValue: values,
        validations,
      },
    });

    await wrapper.find('input[type=radio][value=B]').setValue();
    await wrapper.find('input[type=radio][value=B]').trigger('change');

    const event = wrapper.emitted("update:modelValue");
    expect(event).toBeTruthy();
    expect(event).toHaveLength(1);
    expect(event[0]).toStrictEqual(['B']);
  });

  it('emits the "error" then no error', async () => {

    const wrapper = mount(RadioInputComponent, {
      props: {
        id,
        modelValue: values,
        validations,
      },
    });

    await wrapper.find('input[type=radio][value=A]').setValue();
    await wrapper.find('input[type=radio][value=A]').trigger('change');

    const errorEvent = wrapper.emitted("error");
    const updateEvent = wrapper.emitted("update:modelValue");

    expect(errorEvent).toBeTruthy();
    expect(errorEvent).toHaveLength(1);
    expect(errorEvent[0][0]).toBe("Can't select A");

    expect(updateEvent).toBeTruthy();
    expect(updateEvent).toHaveLength(1);
    expect(updateEvent[0]).toStrictEqual(['A']);

    await wrapper.find('input[type=radio][value=B]').setValue();
    await wrapper.find('input[type=radio][value=B]').trigger('change');

    expect(errorEvent).toHaveLength(2);
    expect(errorEvent[0][1]).toBeUndefined()

    expect(updateEvent).toHaveLength(2);
    expect(updateEvent[1]).toStrictEqual(['B']);

  });
});