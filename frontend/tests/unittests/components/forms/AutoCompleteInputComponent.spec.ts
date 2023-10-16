import { describe, it, expect, beforeEach } from 'vitest'

import { VueWrapper, mount } from '@vue/test-utils'
import AutoCompleteInputComponent from '@/components/forms/AutoCompleteInputComponent.vue'
import CDSComboBox from "@carbon/web-components/es/components/combo-box/combo-box"
import { isMinSize } from '@/helpers/validators/GlobalValidators'

describe('Auto Complete Input Component', () => {
  const id = 'my-input'
  const validations = [(value: any) => (value ? '' : 'Field is required')]
  const contents = [
    { code: 'TA', name: 'TANGO' },
    { code: 'TB', name: 'TAMBOR' },
    { code: 'TC', name: 'TAMCADA' },
    { code: 'TD', name: 'TADANARA' }
  ]
  const eventContent = (value:string) => {return { detail: { item: { 'data-id': value, getAttribute: (key:string) => value } }}}

  it('renders the input field with the provided id', () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: '',
        contents,
        validations: [],
        label: id
      }
    })

    expect(wrapper.find(`#${id}`).exists()).toBe(true)

  })

  it('emits the "update" event with the updated value', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: '',
        contents,
        validations: [],
        label: id,
        tip: ''
      }
    })

    await wrapper.setProps({ modelValue: 'TANGO' })
    await wrapper.find(`#${id}`).trigger('blur')
    await wrapper.find(`#${id}`).trigger('input')

    expect(wrapper.emitted('update:model-value')).toBeTruthy()
    expect(wrapper.emitted('update:model-value')![0][0]).toEqual('TANGO')
  })

  it('emits the "error" event when there is a validation error', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: '',
        contents,
        validations,
        label: id
      }
    })

    await wrapper.find(`#${id}`).trigger('input')
    await wrapper.find(`#${id}`).trigger('blur')

    expect(wrapper.emitted('error')).toBeTruthy()
    expect(wrapper.emitted('error')![0][0]).toBe('Field is required')
  })

  it('emits the "error" event even when the error message is the same as before', async () => {
    const errorMessage = 'sample error message'
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: '',
        contents,
        validations: [isMinSize(errorMessage)(5)],
        label: id,
      }
    })

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    wrapper.find<CDSComboBox>(`#${id}`).element._filterInputValue = 'a'
    await wrapper.find(`#${id}`).trigger('input')
    await wrapper.find(`#${id}`).trigger('blur')

    expect(wrapper.emitted('error')).toBeTruthy()
    expect(wrapper.emitted('error')).toHaveLength(1)
    expect(wrapper.emitted('error')![0][0]).toBe(errorMessage)

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    wrapper.find<CDSComboBox>(`#${id}`).element._filterInputValue = 'ab' // adds another character
    await wrapper.find(`#${id}`).trigger('input')
    await wrapper.find(`#${id}`).trigger('blur')

    expect(wrapper.emitted('error')).toHaveLength(2)
    expect(wrapper.emitted('error')![1][0]).toBe(errorMessage)
  })

  it('emits the "empty" event when the input field is empty', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: '',
        contents,
        validations: [],
        label: id
      }
    })

    await wrapper.find(`#${id}`).trigger('input')
    await wrapper.find(`#${id}`).trigger('blur')

    expect(wrapper.emitted('empty')).toBeTruthy()
    expect(wrapper.emitted('empty')![0][0]).toBe(true)
  })

  it('emits the "empty" event with false when the input field is not empty', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: '',
        contents,
        validations: [],
        label: id
      }
    })

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    wrapper.find<CDSComboBox>(`#${id}`).element._filterInputValue = 'a'
    await wrapper.find(`#${id}`).trigger('input')
    await wrapper.find(`#${id}`).trigger('blur')

    expect(wrapper.emitted('empty')).toHaveLength(2)
    expect(wrapper.emitted('empty')![1][0]).toBe(false)
  })

  describe('when the contents prop is empty', () => {
    const emptyContents: any[] = []
    it('emits "empty" when the input becomes empty', async () => {
      const wrapper = mount(AutoCompleteInputComponent, {
        props: {
          id,
          modelValue: 'a',
          contents: emptyContents,
          validations: [],
          label: id,
        },
      })

      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      wrapper.find<CDSComboBox>(`#${id}`).element._filterInputValue = ''
      await wrapper.find(`#${id}`).trigger('input')

      expect(wrapper.emitted('empty')).toBeTruthy()

      // the first event is from the initialization
      expect(wrapper.emitted('empty')![0][0]).toBe(false)

      // the second event is the result from changing the input value
      // Note: before FSADT1-912, it would emit a "not empty" (false) one.
      expect(wrapper.emitted('empty')![1][0]).toBe(true)

      expect(wrapper.emitted('empty')).toHaveLength(2)
    })
  })

  it('emits the "update:selected-value" event when an option from the list is clicked', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: '',
        contents,
        validations: [],
        label: id
      }
    })

    await wrapper.find(`#${id}`).trigger('cds-combo-box-selected',eventContent('TA'));
    expect(wrapper.emitted('update:selected-value')).toBeTruthy()
    expect(wrapper.emitted('update:selected-value')![0][0]).toEqual(contents[0])
  })

  describe('when an option was previously selected', () => {
    let wrapper: VueWrapper<any, any>

    beforeEach(async () => {
      wrapper = mount(AutoCompleteInputComponent, {
        props: {
          id,
          modelValue: '',
          contents,
          validations: [],
          label: id
        }
      })

      await wrapper.find(`#${id}`).trigger('cds-combo-box-selected',eventContent(contents[0].code));

      // Now an option is effectively selected
      expect(wrapper.emitted('update:selected-value')).toBeTruthy()
      expect(wrapper.emitted('update:selected-value')![0][0]).toEqual(
        contents[0]
      )
    })

    it('emits the "update:selected-value" with undefined when user types something in the input field', async () => {
      // adding a 'Z' character to the end of the original value so to trigger an update:model-value
      await wrapper.setProps({ modelValue: 'TANGOZ' })

      expect(wrapper.emitted('update:selected-value')).toHaveLength(2)
      expect(wrapper.emitted('update:selected-value')![1][0]).toEqual(undefined)
    })

    it('emits the "update:selected-value" with the newly selected value', async () => {
      await wrapper.find(`#${id}`).trigger('cds-combo-box-selected',eventContent(contents[1].code));

      expect(wrapper.emitted('update:selected-value')).toHaveLength(2)
      expect(wrapper.emitted('update:selected-value')![1][0]).toEqual(
        contents[1]
      )
    })
  })
})
