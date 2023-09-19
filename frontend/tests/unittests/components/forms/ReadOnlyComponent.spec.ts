import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ReadOnlyComponent from '@/components/forms/ReadOnlyComponent.vue'


describe('Read Only Component', () => {
  it('renders the provided value', () => {
    const wrapper = mount(ReadOnlyComponent, {
      props: {
        label: 'John Doe'
      },
      slots: {
        default: '<p>You cant touch me</p>'
      }
    })

    expect(wrapper.find('span.label-01').text()).toBe('John Doe')
    expect(wrapper.find('p').text()).toBe('You cant touch me')
  })
})
