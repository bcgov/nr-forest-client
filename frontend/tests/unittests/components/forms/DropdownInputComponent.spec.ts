import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'

import DropdownInputComponent from '@/components/forms/DropdownInputComponent.vue'

describe('DropdownInputComponent', () => {
  const validations = [
    (value: any) => (value === 'A' ? 'A is not supported' : '')
  ]

  it('should render', () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: 'test',
        label: 'test',
        tip: '',
        modelValue: [
          { code: 'A', name: 'Value A' },
          { code: 'B', name: 'Value B' }
        ],
        initialValue: '',
        validations: []
      }
    })

    expect(wrapper.find('bx-dropdown-item[value="A"]').exists()).toBe(true)
    expect(wrapper.find('bx-dropdown-item[value="B"]').exists()).toBe(true)
    expect(wrapper.find('bx-dropdown-item[value="C"]').exists()).toBe(false)
  })

  it('should emit event when changing selection', async () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: 'test',
        label: 'test',
        tip: '',
        modelValue: [
          { code: 'A', name: 'Value A' },
          { code: 'B', name: 'Value B' }
        ],
        initialValue: '',
        validations: []
      }
    })

    const dropdown = wrapper.find('bx-dropdown')

    await dropdown.trigger('bx-dropdown-beingselected', {
      detail: { item: { __value: 'A' } }
    })

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0][0]).toBe('A')

    expect(wrapper.emitted('update:selectedValue')).toBeTruthy()
    expect(wrapper.emitted('update:selectedValue')![0][0]).toStrictEqual({
      code: 'A',
      name: 'Value A'
    })
  })

  it('should emit empty then emit not empty', async () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: 'test',
        label: 'test',
        tip: '',
        modelValue: [
          { code: 'A', name: 'Value A' },
          { code: 'B', name: 'Value B' }
        ],
        initialValue: '',
        validations: []
      }
    })

    expect(wrapper.emitted('empty')).toBeTruthy()
    expect(wrapper.emitted('empty')![0][0]).toBe(true)

    const dropdown = wrapper.find('bx-dropdown')

    await dropdown.trigger('bx-dropdown-beingselected', {
      detail: { item: { __value: 'A' } }
    })

    expect(wrapper.emitted('empty')).toBeTruthy()
    expect(wrapper.emitted('empty')![1][0]).toBe(false)
  })

  it('should validate and emit error if required', async () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: 'test',
        label: 'test',
        tip: '',
        modelValue: [
          { code: 'A', name: 'Value A' },
          { code: 'B', name: 'Value B' }
        ],
        initialValue: '',
        validations
      }
    })

    const dropdown = wrapper.find('bx-dropdown')

    await dropdown.trigger('bx-dropdown-beingselected', {
      detail: { item: { __value: 'A' } }
    })

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0][0]).toBe('A')

    expect(wrapper.emitted('update:selectedValue')).toBeTruthy()
    expect(wrapper.emitted('update:selectedValue')![0][0]).toStrictEqual({
      code: 'A',
      name: 'Value A'
    })

    expect(wrapper.emitted('error')).toBeTruthy()
    expect(wrapper.emitted('error')![0][0]).toBe('A is not supported')
  })

  it('should validate and emit no error if required', async () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: 'test',
        label: 'test',
        tip: '',
        modelValue: [
          { code: 'A', name: 'Value A' },
          { code: 'B', name: 'Value B' }
        ],
        initialValue: '',
        validations
      }
    })

    const dropdown = wrapper.find('bx-dropdown')

    await dropdown.trigger('bx-dropdown-beingselected', {
      detail: { item: { __value: 'B' } }
    })

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0][0]).toBe('B')

    expect(wrapper.emitted('update:selectedValue')).toBeTruthy()
    expect(wrapper.emitted('update:selectedValue')![0][0]).toStrictEqual({
      code: 'B',
      name: 'Value B'
    })

    expect(wrapper.emitted('error')).toBeTruthy()
    expect(wrapper.emitted('error')![0][0]).toBe(undefined)
  })

  it('should reset selected to initial value when list change', async () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: 'test',
        label: 'test',
        tip: '',
        modelValue: [
          { code: 'A', name: 'Value A' },
          { code: 'B', name: 'Value B' }
        ],
        initialValue: '',
        validations: []
      }
    })

    const dropdown = wrapper.find('bx-dropdown')

    await dropdown.trigger('bx-dropdown-beingselected', {
      detail: { item: { __value: 'A' } }
    })

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0][0]).toBe('A')

    expect(wrapper.emitted('update:selectedValue')).toBeTruthy()
    expect(wrapper.emitted('update:selectedValue')![0][0]).toStrictEqual({
      code: 'A',
      name: 'Value A'
    })

    await wrapper.setProps({
      modelValue: [
        { code: 'C', name: 'Value C' },
        { code: 'D', name: 'Value D' }
      ]
    })

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![1][0]).toBe('')

    expect(wrapper.emitted('update:selectedValue')).toBeTruthy()
    expect(wrapper.emitted('update:selectedValue')![1][0]).toStrictEqual(
      undefined
    )
  })
})
