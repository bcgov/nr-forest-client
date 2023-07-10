import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import MultiselectInputComponent from '@/components/forms/MultiselectInputComponent.vue'

describe('MultiselectInputComponent', () => {
  const validations = [
    (value: any) =>
      value === 'A' || value === 'Value A' ? 'A is not supported' : ''
  ]

  it('should render', () => {
    const wrapper = mount(MultiselectInputComponent, {
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
    const wrapper = mount(MultiselectInputComponent, {
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

    await dropdown.trigger('bx-dropdown-selected', {
      detail: { item: { __value: 'A' } }
    })

    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0][0]).toStrictEqual([
      'Value A'
    ])

    expect(wrapper.emitted('update:selectedValue')).toBeTruthy()
    expect(wrapper.emitted('update:selectedValue')![0][0]).toStrictEqual([
      {
        code: 'A',
        name: 'Value A'
      }
    ])
  })

  it('should emit empty then emit not empty', async () => {
    const wrapper = mount(MultiselectInputComponent, {
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

    await dropdown.trigger('bx-dropdown-selected', {
      detail: { item: { __value: 'A' } }
    })

    expect(wrapper.emitted('empty')).toBeTruthy()
    expect(wrapper.emitted('empty')![1][0]).toBe(false)
  })

  it('should validate and emit error if required', async () => {
    const wrapper = mount(MultiselectInputComponent, {
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

    await dropdown.trigger('bx-dropdown-selected', {
      detail: { item: { __value: 'A' } }
    })
    await dropdown.trigger('bx-dropdown-beingselected', {
      detail: { item: { __value: 'A' } }
    })

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0][0]).toStrictEqual([
      'Value A'
    ])

    expect(wrapper.emitted('update:selectedValue')).toBeTruthy()
    expect(wrapper.emitted('update:selectedValue')![0][0]).toStrictEqual([
      {
        code: 'A',
        name: 'Value A'
      }
    ])

    expect(wrapper.emitted('error')).toBeTruthy()
    expect(wrapper.emitted('error')![1][0]).toBe('A is not supported')
  })

  it('should validate and emit no error if required', async () => {
    const wrapper = mount(MultiselectInputComponent, {
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

    await dropdown.trigger('bx-dropdown-selected', {
      detail: { item: { __value: 'B' } }
    })

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0][0]).toStrictEqual([
      'Value B'
    ])

    expect(wrapper.emitted('update:selectedValue')).toBeTruthy()
    expect(wrapper.emitted('update:selectedValue')![0][0]).toStrictEqual([
      {
        code: 'B',
        name: 'Value B'
      }
    ])

    expect(wrapper.emitted('error')).toBeTruthy()
    expect(wrapper.emitted('error')![0][0]).toBe(undefined)
  })

  it('should select and emit on initial values', async () => {
    const wrapper = mount(MultiselectInputComponent, {
      props: {
        id: 'test',
        label: 'test',
        tip: '',
        modelValue: [
          { code: 'A', name: 'Value A' },
          { code: 'B', name: 'Value B' }
        ],
        initialValue: '',
        validations: [],
        selectedValues: ['A']
      }
    })

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0][0]).toStrictEqual([
      'Value A'
    ])

    expect(wrapper.emitted('update:selectedValue')).toBeTruthy()
    expect(wrapper.emitted('update:selectedValue')![0][0]).toStrictEqual([
      {
        code: 'A',
        name: 'Value A'
      }
    ])
  })

  it('should remove selection', async () => {
    const wrapper = mount(MultiselectInputComponent, {
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

    await dropdown.trigger('bx-dropdown-selected', {
      detail: { item: { __value: 'A' } }
    })

    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0][0]).toStrictEqual([
      'Value A'
    ])

    expect(wrapper.emitted('update:selectedValue')).toBeTruthy()
    expect(wrapper.emitted('update:selectedValue')![0][0]).toStrictEqual([
      {
        code: 'A',
        name: 'Value A'
      }
    ])

    const close = wrapper.find('#close_test_0')

    await close.trigger('click')

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![1][0]).toStrictEqual([])

    expect(wrapper.emitted('update:selectedValue')).toBeTruthy()
    expect(wrapper.emitted('update:selectedValue')![1][0]).toStrictEqual([])
  })
})
