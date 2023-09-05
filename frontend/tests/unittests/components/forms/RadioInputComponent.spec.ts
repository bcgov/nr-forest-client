import { describe, it, expect } from 'vitest'

import { mount } from '@vue/test-utils'
import RadioInputComponent from '@/components/forms/RadioInputComponent.vue'

describe('Radio Input Component', () => {
  const id = 'my-input'
  const values = [
    { value: 'A', text: 'First' },
    { value: 'B', text: 'Second' }
  ]
  const validations = [
    (value: any) => (value && value === 'A' ? "Can't select A" : '')
  ]

  it('renders the input field with the provided id', () => {
    const wrapper = mount(RadioInputComponent, {
      props: {
        id,
        label: 'Options',
        modelValue: values,
        validations: [],
        initialValue: ''
      }
    })
    for (const value of values) {
      expect(wrapper.html()).toContain(
        `<cds-radio-button label-text="${value.text}"></cds-radio-button>`
      )
    }
  })

  it('emits the "error" event when there is a validation error', async () => {
    const wrapper = mount(RadioInputComponent, {
      props: {
        id,
        label: 'Options',
        modelValue: values,
        validations,
        initialValue: ''
      }
    })

    await wrapper
      .find(`[id=${id}rb]`)
      .trigger('cds-radio-button-group-changed', {
        detail: { value: 'A' }
      })

    const event = wrapper.emitted('error')

    expect(event).toBeTruthy()
    expect(event).toHaveLength(1)
    expect(event[0][0]).toBe("Can't select A")
  })

  it('emits the "update" event when selected', async () => {
    const wrapper = mount(RadioInputComponent, {
      props: {
        id,
        label: 'Options',
        modelValue: values,
        validations,
        initialValue: ''
      }
    })

    await wrapper
      .find(`[id=${id}rb]`)
      .trigger('cds-radio-button-group-changed', {
        detail: { value: 'B' }
      })

    const event = wrapper.emitted('update:model-value')
    expect(event).toBeTruthy()
    expect(event).toHaveLength(1)
    expect(event[0]).toStrictEqual(['B'])
  })

  it('emits the "error" then no error', async () => {
    const wrapper = mount(RadioInputComponent, {
      props: {
        id,
        label: 'Options',
        modelValue: values,
        validations,
        initialValue: ''
      }
    })

    await wrapper
      .find(`[id=${id}rb]`)
      .trigger('cds-radio-button-group-changed', {
        detail: { value: 'A' }
      })

    const errorEvent = wrapper.emitted('error')
    const updateEvent = wrapper.emitted('update:model-value')

    expect(errorEvent).toBeTruthy()
    expect(errorEvent).toHaveLength(1)
    expect(errorEvent[0][0]).toBe("Can't select A")

    expect(updateEvent).toBeTruthy()
    expect(updateEvent).toHaveLength(1)
    expect(updateEvent[0]).toStrictEqual(['A'])

    await wrapper
      .find(`[id=${id}rb]`)
      .trigger('cds-radio-button-group-changed', {
        detail: { value: 'B' }
      })

    expect(errorEvent).toHaveLength(2)
    expect(errorEvent[0][1]).toBeUndefined()

    expect(updateEvent).toHaveLength(2)
    expect(updateEvent[1]).toStrictEqual(['B'])
  })
})
