import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import MultiselectInputComponent from '@/components/forms/MultiselectInputComponent.vue'

describe('MultiselectInputComponent', () => {
  const validations = [
    (value: any[]) =>
      value.includes('A') || value.includes('Value A') ? 'A is not supported' : ''
  ]

  const eventContent = (value: string) => {
    return { data: value }
  }

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

    expect(wrapper.find('cds-multi-select-item[data-value="Value A"]').exists()).toBe(true)
    expect(wrapper.find('cds-multi-select-item[data-value="Value B"]').exists()).toBe(true)
    expect(wrapper.find('cds-multi-select-item[data-value="Value C"]').exists()).toBe(false)
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

    const dropdown = wrapper.find('cds-multi-select')

    await dropdown.trigger('cds-multi-select-selected',eventContent('Value A'))

    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0][0]).toStrictEqual(['Value A'])

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

    const dropdown = wrapper.find('cds-multi-select')

    await dropdown.trigger('cds-multi-select-selected',eventContent('Value A'))

    expect(wrapper.emitted('empty')).toBeTruthy()
    expect(wrapper.emitted('empty')![1][0]).toBe(false)
  })

  it('should emit not empty when selectedValues is not empty', async () => {
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
        selectedValues: ['Value A']
      }
    })

    expect(wrapper.emitted('empty')).toBeTruthy()
    expect(wrapper.emitted('empty')![0][0]).toBe(false)
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

    const dropdown = wrapper.find('cds-multi-select')

    await dropdown.trigger('cds-multi-select-selected', eventContent('Value A'))

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0][0]).toStrictEqual(['Value A'])

    expect(wrapper.emitted('update:selectedValue')).toBeTruthy()
    expect(wrapper.emitted('update:selectedValue')![0][0]).toStrictEqual([
      {
        code: 'A',
        name: 'Value A'
      }
    ])

    expect(wrapper.emitted('error')).toBeTruthy()
    expect(wrapper.emitted('error')![0][0]).toBe('A is not supported')
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

    const dropdown = wrapper.find('cds-multi-select')

    await dropdown.trigger('cds-multi-select-selected', eventContent('Value B'))

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0][0]).toStrictEqual(['Value B'])

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

    const dropdown = wrapper.find('#test')

    await dropdown.trigger('cds-multi-select-selected',eventContent('Value A'))

    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0][0]).toStrictEqual(['Value A'])

    expect(wrapper.emitted('update:selectedValue')).toBeTruthy()
    expect(wrapper.emitted('update:selectedValue')![0][0]).toStrictEqual([ { code: 'A', name: 'Value A' } ])

    await dropdown.trigger('cds-multi-select-selected',eventContent(''))

    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![1][0]).toStrictEqual([])

    expect(wrapper.emitted('update:selectedValue')).toBeTruthy()
    expect(wrapper.emitted('update:selectedValue')![1][0]).toStrictEqual([])
  })
})
