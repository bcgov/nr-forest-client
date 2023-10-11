import { describe, it, expect } from 'vitest'
import { DOMWrapper, mount } from '@vue/test-utils'
import TextInputComponent from '@/components/forms/TextInputComponent.vue'
import { isMinSize } from "@/helpers/validators/GlobalValidators"

describe('Text Input Component', () => {
  const id = 'my-input'
  const validations = [(value: any) => (value ? '' : 'Field is required')]

  const setInputValue = async (inputWrapper: DOMWrapper<HTMLInputElement>, value: string) => {
    inputWrapper.element.value = value
    await inputWrapper.trigger('input')
  }

  it('renders the input field with the provided id', () => {
    const wrapper = mount(TextInputComponent, {
      props: {
        id,
        label: 'TestField',
        placeholder: '',
        modelValue: '',
        validations: [],
        enabled: true
      },
      directives: {
        masked: () => {}
      }
    })

    expect(wrapper.find(`#${id}`).exists()).toBe(true)
  })

  it('emits the "update" event with the updated value', async () => {
    const wrapper = mount(TextInputComponent, {
      props: {
        id,
        label: 'TestField',
        placeholder: '',
        modelValue: '',
        validations: [],
        enabled: true
      },
      directives: {
        masked: () => {}
      }
    })

    const inputWrapper = wrapper.find<HTMLInputElement>(`#${id}`)
    await setInputValue(inputWrapper, 'John Doe')
    await inputWrapper.trigger('blur')

    expect(wrapper.emitted('update:model-value')).toBeTruthy()
    expect(wrapper.emitted('update:model-value')![0][0]).toEqual('John Doe')
  })

  it('emits the "error" event when there is a validation error', async () => {
    const wrapper = mount(TextInputComponent, {
      props: {
        id,
        label: 'TestField',
        placeholder: '',
        modelValue: '',
        validations,
        enabled: true
      },
      directives: {
        masked: () => {}
      }
    })

    await wrapper.find(`#${id}`).trigger('input')
    await wrapper.find(`#${id}`).trigger('blur')

    expect(wrapper.emitted('error')).toBeTruthy()
    expect(wrapper.emitted('error')![0][0]).toBe('Field is required')
  })

  it('revalidates when changed from outside (i.e. not directly by the user)', async () => {
    const errorMessage = "sample error message";
    const wrapper = mount(TextInputComponent, {
      props: {
        id,
        label: 'TestField',
        placeholder: '',
        modelValue: '',
        validations: [isMinSize(errorMessage)(5)],
        enabled: true,
      },
      directives: {
        masked: () => {},
      },
    });

    /*
    Note: we are not triggerring "input" nor "blur".
    Just like on a programmatic change, which was not performed by a user.
    */
    await wrapper.setProps({ modelValue: "1"});

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")).toHaveLength(1);
    expect(wrapper.emitted("error")![0][0]).toBe(errorMessage);
  });

  it('emits the "error" event even when the error message is the same as before', async () => {
    const errorMessage = "sample error message";
    const wrapper = mount(TextInputComponent, {
      props: {
        id,
        label: 'TestField',
        placeholder: '',
        modelValue: '',
        validations: [isMinSize(errorMessage)(5)],
        enabled: true,
      },
      directives: {
        masked: () => {},
      },
    });

    const inputWrapper = wrapper.find<HTMLInputElement>(`#${id}`)
    await setInputValue(inputWrapper, '1')
    await inputWrapper.trigger('blur')

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")).toHaveLength(1);
    expect(wrapper.emitted("error")![0][0]).toBe(errorMessage);

    await setInputValue(inputWrapper, '12') // adds another character
    await inputWrapper.trigger('blur')

    // Just a sanity check on the updated element value
    expect(inputWrapper.element.value).toEqual('12')

    expect(wrapper.emitted("error")).toHaveLength(2);
    expect(wrapper.emitted("error")![1][0]).toBe(errorMessage);
  });

  it('emits the "empty" event when the input field is empty', async () => {
    const wrapper = mount(TextInputComponent, {
      props: {
        id,
        label: 'TestField',
        placeholder: '',
        modelValue: '',
        validations: [],
        enabled: true
      },
      directives: {
        masked: () => {}
      }
    })

    await wrapper.find(`#${id}`).trigger('input')
    await wrapper.find(`#${id}`).trigger('blur')

    expect(wrapper.emitted('empty')).toBeTruthy()
    expect(wrapper.emitted('empty')![0][0]).toBe(true)
  })
})
