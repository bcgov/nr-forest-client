import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'

import MainHeaderComponent from '@/components/MainHeaderComponent.vue'

describe('MainHeaderComponent.vue', () => {
  it('renders the component when authenticated', async () => {
    const mockSession = {
      user: {},
      logOut: vi.fn()
    }

    const wrapper = mount(MainHeaderComponent, {
      global: {
        mocks: {
          $session: mockSession
        }
      }
    })

    expect(wrapper.find('header').exists()).toBe(true)
    expect(wrapper.find('bx-btn').exists()).toBe(true)
    expect(wrapper.html()).toContain('Ministry of Forests')
  })

  it('renders the component when unauthenticated', async () => {
    const wrapper = mount(MainHeaderComponent, {
      global: {
        mocks: {
          $session: null
        }
      }
    })

    expect(wrapper.find('header').exists()).toBe(true)
    expect(wrapper.find('bx-btn').exists()).toBe(false)
    expect(wrapper.html()).toContain('Ministry of Forests')
  })
})
